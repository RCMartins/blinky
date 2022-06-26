package blinky.run

import os.{Path, RelPath}
import blinky.BuildInfo
import blinky.run.Instruction._
import blinky.run.config.{FileFilter, MutationsConfigValidated, SimpleBlinkyConfig}
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
            runSyncEither("git", Seq("rev-parse", "--show-toplevel"), path = originalProjectRoot)
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
                        // cp(gitFolder / ".git", cloneProjectBaseFolder / ".git")
                        runSyncEither("git", Seq("rev-parse", "master"), path = gitFolder)
                          .flatMap {
                            case Left(commandError) =>
                              ConsoleReporter
                                .gitIssues(commandError)
                                .map(_ => Left(ExitCode.failure))
                            case Right(masterHash) =>
                              runSyncEither(
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

                                  if (base.isEmpty)
                                    succeed(Right(("", base)))
                                  else
                                    for {
                                      copyResult <- copyFilesToTempFolder(
                                        originalProjectRoot,
                                        originalProjectPath,
                                        projectRealPath
                                      )
                                      result <- optimiseFilesToMutate(
                                        base,
                                        copyResult,
                                        projectRealPath,
                                        config.filesToMutate
                                      )
                                    } yield result
                              }
                          }
                      else
                        for {
                          _ <- copyFilesToTempFolder(
                            originalProjectRoot,
                            originalProjectPath,
                            projectRealPath
                          )
                          processResult <- processFilesToMutate(
                            projectRealPath,
                            config.filesToMutate
                          )
                        } yield processResult.map((_, Seq("all")))
                    }

                    runResult <- filesToMutateEither match {
                      case Left(exitCode) =>
                        succeed(exitCode)
                      case Right((_, Seq())) =>
                        ConsoleReporter.filesToMutateIsEmpty
                          .map(_ => ExitCode.success)
                      case Right((filesToMutateStr, filesToMutateSeq)) =>
                        for {
                          coursier <- Setup.setupCoursier(projectRealPath)
                          _ <- Setup.sbtCompileWithSemanticDB(projectRealPath)
                          _ <- Setup.setupScalafix(projectRealPath)

                          // Setup BlinkyConfig object
                          blinkyConf: BlinkyConfig = BlinkyConfig(
                            mutantsOutputFile = (projectRealPath / "blinky.mutants").toString,
                            filesToMutate = filesToMutateSeq,
                            specificMutants = config.options.mutant,
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

                          runResult <- runSyncEither(
                            coursier,
                            Seq(
                              "fetch",
                              s"com.github.rcmartins:${ruleName.toLowerCase}_${BuildInfo.scalaMinorVersion}:${BuildInfo.version}",
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
                                  s"--files=$filesToMutateStr",
                                  s"--config=$scalafixConfFile",
                                  "--auto-classpath=target"
                                ).filter(_.nonEmpty)
                              for {
                                _ <- printLine(toolPath)
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

  private def filterFiles(
      files: Seq[String],
      fileName: String
  ): Instruction[Either[ExitCode, String]] = {
    val filesFiltered: Seq[String] =
      files.collect { case file if file.endsWith(fileName) => file }
    filesFiltered match {
      case Seq() =>
        printLine(s"--filesToMutate '$fileName' does not exist.")
          .map(_ => Left(ExitCode.failure))
      case Seq(singleFile) =>
        succeed(Right(singleFile))
      case _ =>
        printLine(
          s"""--filesToMutate is ambiguous.
             |Files ending with the same path:
             |${filesFiltered.mkString("\n")}""".stripMargin
        ).map(_ => Left(ExitCode.failure))
    }
  }

  def processFilesToMutate(
      projectRealPath: Path,
      filesToMutate: FileFilter
  ): Instruction[Either[ExitCode, String]] =
    filesToMutate match {
      case FileFilter.SingleFileOrFolder(fileOrFolder) =>
        succeed(Right(fileOrFolder.toString))
      case FileFilter.FileName(fileName) =>
        lsFiles(projectRealPath).flatMap(filterFiles(_, fileName))
    }

  def optimiseFilesToMutate(
      base: Seq[String],
      copyResult: Either[ExitCode, Unit],
      projectRealPath: Path,
      filesToMutate: FileFilter
  ): Instruction[Either[ExitCode, (String, Seq[String])]] =
    copyResult match {
      case Left(result) =>
        succeed(Left(result))
      case Right(_) => // This part is just an optimization of 'base'
        val fileToMutateInst: Instruction[Either[ExitCode, Path]] =
          filesToMutate match {
            case FileFilter.SingleFileOrFolder(fileOrFolder) =>
              succeed(Right(projectRealPath / fileOrFolder))
            case FileFilter.FileName(fileName) =>
              filterFiles(base, fileName).map(_.map(Path(_)))
          }

        for {
          fileToMutateResult <- fileToMutateInst
          result <-
            fileToMutateResult match {
              case Left(exitCode) =>
                succeed(Left(exitCode))
              case Right(configFileOrFolderToMutate) =>
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
                    succeed(base.filter(_.startsWith(configFileOrFolderToMutateStr)))
                ).map(baseFiltered => Right((configFileOrFolderToMutateStr, baseFiltered)))
            }
        } yield result
    }

  def copyFilesToTempFolder(
      originalProjectRoot: Path,
      originalProjectPath: Path,
      projectRealPath: Path
  ): Instruction[Either[ExitCode, Unit]] =
    for {
      // Copy only the files tracked by git into our temporary folder
      gitResultEither <- runSyncEither(
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
