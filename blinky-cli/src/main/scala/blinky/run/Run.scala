package blinky.run

import ammonite.ops.{Path, RelPath}
import blinky.BuildInfo
import blinky.run.Instruction._
import blinky.run.config.{MutationsConfigValidated, SimpleBlinkyConfig}
import blinky.run.modules.CliModule
import blinky.v0.BlinkyConfig
import zio.{ExitCode, RIO}

import scala.util.Try

object Run {

  private val ruleName = "Blinky"

  def run(config: MutationsConfigValidated): RIO[CliModule, Instruction[ExitCode]] =
    for {
      pwd <- CliModule.pwd

      originalProjectRoot = Path(pwd.path.toAbsolutePath)
      originalProjectRelPath =
        Try(Path(config.projectPath.pathAsString).relativeTo(originalProjectRoot))
          .getOrElse(RelPath(config.projectPath.pathAsString))
      originalProjectPath = originalProjectRoot / originalProjectRelPath

      instruction = {
        for {
          cloneProjectTempFolder <- makeTemporaryFolder
          _ <-
            if (config.options.verbose)
              printLine(s"Temporary project folder: $cloneProjectTempFolder")
            else
              empty
          runResult <-
            runAsync("git", Seq("rev-parse", "--show-toplevel"), path = originalProjectRoot)
              .flatMap {
                case Left(commandError) =>
                  ConsoleReporter
                    .gitIssues(commandError)
                    .map(_ => ExitCode.failure)
                case Right(gitRevParse) =>
                  val gitFolder = Path(gitRevParse)
                  val cloneProjectBaseFolder: Path = cloneProjectTempFolder / gitFolder.baseName
                  val projectRealRelPath: RelPath = originalProjectPath.relativeTo(gitFolder)
                  val projectRealPath: Path = cloneProjectBaseFolder / projectRealRelPath

                  for {
                    _ <- makeDirectory(cloneProjectBaseFolder)

                    // Setup files to mutate ('scalafix --diff' does not work like I want...)
                    filesToMutateEither <- {
                      if (config.options.onlyMutateDiff)
                        // maybe copy the .git folder so it can be used by TestMutations, etc?
                        //cp(gitFolder / ".git", cloneProjectBaseFolder / ".git")
                        runAsync("git", Seq("rev-parse", "master"), path = gitFolder)
                          .flatMap {
                            case Left(commandError) =>
                              ConsoleReporter
                                .gitIssues(commandError)
                                .map(_ => Left(ExitCode.failure))
                            case Right(masterHash) =>
                              runAsync(
                                "git",
                                Seq("--no-pager", "diff", "--name-only", masterHash),
                                path = gitFolder
                              ).flatMap {
                                case Left(commandError) =>
                                  ConsoleReporter
                                    .gitIssues(commandError)
                                    .map(_ => Left(ExitCode.failure))
                                case Right(diffLines) =>
                                  val base: Seq[String] =
                                    diffLines
                                      .split(System.lineSeparator())
                                      .toSeq
                                      .map(file => cloneProjectBaseFolder / RelPath(file))
                                      .filter(file => file.ext == "scala" || file.ext == "sbt")
                                      .map(_.toString)

                                  for {
                                    result <-
                                      if (base.isEmpty)
                                        succeed(Right(base))
                                      else
                                        for {
                                          copyResult <- copyFilesToTempFolder(
                                            originalProjectRoot,
                                            originalProjectPath,
                                            projectRealPath
                                          )
                                          result <- copyResult match {
                                            case Left(result) =>
                                              succeed(Left(result))
                                            case Right(_) =>
                                              // This part is just an optimization of 'base'
                                              val configFileOrFolderToMutate: Path =
                                                Try(Path(config.filesToMutate))
                                                  .getOrElse(
                                                    projectRealPath / RelPath(config.filesToMutate)
                                                  )

                                              val configFileOrFolderToMutateStr =
                                                configFileOrFolderToMutate.toString

                                              IsFile(
                                                configFileOrFolderToMutate,
                                                if (_)
                                                  if (base.contains(configFileOrFolderToMutateStr))
                                                    succeed(Seq(configFileOrFolderToMutateStr))
                                                  else
                                                    succeed(Seq.empty[String])
                                                else
                                                  succeed(
                                                    base.filter(
                                                      _.startsWith(configFileOrFolderToMutateStr)
                                                    )
                                                  )
                                              ).map(Right(_))
                                          }
                                        } yield result
                                  } yield result
                              }
                          }
                      else
                        copyFilesToTempFolder(
                          originalProjectRoot,
                          originalProjectPath,
                          projectRealPath
                        ).map(_ => Right(Seq("all")))
                    }

                    runResult <- filesToMutateEither match {
                      case Left(exitCode) =>
                        succeed(exitCode)
                      case Right(Seq()) =>
                        ConsoleReporter.filesToMutateIsEmpty
                          .map(_ => ExitCode.success)
                      case Right(filesToMutate) =>
                        for {
                          coursier <- Setup.setupCoursier(projectRealPath)
                          _ <- Setup.sbtCompileWithSemanticDB(projectRealPath)
                          _ <- Setup.setupScalafix(projectRealPath)

                          // Setup BlinkyConfig object
                          blinkyConf: BlinkyConfig = BlinkyConfig(
                            mutantsOutputFile = (projectRealPath / "blinky.mutants").toString,
                            filesToMutate = filesToMutate,
                            enabledMutators = config.mutators.enabled,
                            disabledMutators = config.mutators.disabled
                          )

                          // Setup our .blinky.scalafix.conf file to be used by Blinky rule
                          scalafixConfFile <- {
                            val scalaFixConf =
                              SimpleBlinkyConfig.blinkyConfigEncoder.write(blinkyConf).show.trim

                            val confFile = cloneProjectTempFolder / ".scalafix.conf"
                            WriteFile(
                              confFile,
                              s"""rules = $ruleName
                                 |Blinky $scalaFixConf""".stripMargin,
                              succeed(confFile)
                            )
                          }

                          runResult <- runAsync(
                            coursier,
                            Seq(
                              "fetch",
                              s"com.github.rcmartins:${ruleName.toLowerCase}_2.12:${BuildInfo.version}",
                              "-p"
                            ),
                            Map(
                              "COURSIER_REPOSITORIES" -> "ivy2Local|sonatype:snapshots|sonatype:releases"
                            ),
                            path = projectRealPath
                          ).flatMap {
                            case Left(commandError) =>
                              ConsoleReporter
                                .gitIssues(commandError)
                                .map(_ => ExitCode.failure)
                            case Right(toolPath) =>
                              val params: Seq[String] =
                                Seq(
                                  if (config.options.verbose) "--verbose" else "",
                                  if (config.filesToExclude.nonEmpty)
                                    s"--exclude=${config.filesToExclude}"
                                  else "",
                                  s"--tool-classpath=$toolPath",
                                  s"--files=${config.filesToMutate}",
                                  s"--config=$scalafixConfFile",
                                  "--auto-classpath=target"
                                ).filter(_.nonEmpty)
                              for {
                                _ <- runSync("./scalafix", params, path = projectRealPath)
                                runResult <- TestMutationsBloop.run(
                                  projectRealPath,
                                  blinkyConf,
                                  config.options
                                )
                              } yield runResult
                          }
                        } yield runResult
                    }
                  } yield runResult
              }
        } yield runResult
      }
    } yield instruction

  def copyFilesToTempFolder(
      originalProjectRoot: Path,
      originalProjectPath: Path,
      projectRealPath: Path
  ): Instruction[Either[ExitCode, Unit]] =
    for {
      // Copy only the files tracked by git into our temporary folder
      gitResultEither <- runAsync(
        "git",
        Seq("ls-files", "--others", "--exclude-standard", "--cached"),
        path = originalProjectPath
      )
      result <- gitResultEither match {
        case Left(commandError) =>
          ConsoleReporter
            .gitIssues(commandError)
            .map(_ => Left(ExitCode.failure))
        case Right(gitResult) =>
          val filesToCopy: Seq[RelPath] =
            gitResult.split(System.lineSeparator()).map(RelPath(_)).toSeq
          for {
            copyResult <- copyRelativeFiles(
              filesToCopy,
              originalProjectRoot,
              projectRealPath
            )
            _ <- copyResult match {
              case Left(error) => printLine(s"Error copying project files: $error")
              case Right(())   => succeed(())
            }
          } yield Right(())
      }
    } yield result

}
