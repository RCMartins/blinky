package blinky.run

import ammonite.ops.{Path, RelPath, statFileData, up}
import blinky.BuildInfo
import blinky.run.Instruction._
import blinky.run.config.{MutationsConfigValidated, SimpleBlinkyConfig}
import blinky.run.modules.CliModule
import blinky.v0.BlinkyConfig
import zio.{ExitCode, URIO, ZIO}

import scala.util.Try

object Run {

  private val ruleName = "Blinky"

  def run(config: MutationsConfigValidated): URIO[CliModule, Instruction[ExitCode]] =
    for {
      env <- ZIO.environment[CliModule]
      pwd <- env.cliModule.pwd

      originalProjectRoot: Path = Path(pwd.path.toAbsolutePath)
      originalProjectRelPath: RelPath =
        Try(Path(config.projectPath.pathAsString).relativeTo(originalProjectRoot))
          .getOrElse(RelPath(config.projectPath.pathAsString))
      originalProjectPath: Path = originalProjectRoot / originalProjectRelPath

      inst = {
        for {
          cloneProjectTempFolder <- makeTemporaryFolder
          _ <-
            if (config.options.verbose)
              printLine(s"Temporary project folder: $cloneProjectTempFolder")
            else
              empty

          gitResult <- runAsync("git", Seq("rev-parse", "--show-toplevel"))(
            originalProjectRoot
          )
          gitFolder = Path(gitResult)

          cloneProjectBaseFolder: Path = cloneProjectTempFolder / gitFolder.baseName
          _ <- makeDirectory(cloneProjectBaseFolder)
          projectRealRelPath: RelPath = originalProjectPath.relativeTo(gitFolder)
          projectRealPath: Path = cloneProjectBaseFolder / projectRealRelPath

          copyFilesToTempFolder: Instruction[Unit] = for {
            // Copy only the files tracked by git into our temporary folder
            gitResult <- runAsync(
              "git",
              Seq("ls-files", "--others", "--exclude-standard", "--cached")
            )(originalProjectPath)
            filesToCopy = gitResult.split(System.lineSeparator()).map(RelPath(_))

            _ <- filesToCopy.foldLeft(succeed(()): Instruction[Unit]) { (before, fileToCopy) =>
              before.flatMap(_ =>
                MakeDirectory(
                  projectRealPath / fileToCopy / up,
                  copyInto(
                    originalProjectRoot / fileToCopy,
                    projectRealPath / fileToCopy / up
                  )
                )
              )
            }
          } yield ()

          // Setup files to mutate ('scalafix --diff' does not work like I want...)
          filesToMutate <-
            if (config.options.onlyMutateDiff)
              // maybe copy the .git folder so it can be used by TestMutations, etc?
              //cp(gitFolder / ".git", cloneProjectBaseFolder / ".git")
              for {
                masterHash <- runAsync("git", Seq("rev-parse", "master"))(gitFolder)
                diffLines <- runAsync("git", Seq("--no-pager", "diff", "--name-only", masterHash))(
                  gitFolder
                )

                base: Seq[String] =
                  diffLines
                    .split(System.lineSeparator())
                    .toSeq
                    .map(file => cloneProjectBaseFolder / RelPath(file))
                    .filter(file => file.ext == "scala" || file.ext == "sbt")
                    .map(_.toString)

                result <-
                  if (base.isEmpty)
                    succeed(base)
                  else
                    copyFilesToTempFolder.flatMap { (_: Unit) =>
                      // This part is just an optimization of 'base'
                      val configFileOrFolderToMutate: Path =
                        Try(Path(config.filesToMutate))
                          .getOrElse(projectRealPath / RelPath(config.filesToMutate))

                      val configFileOrFolderToMutateStr =
                        configFileOrFolderToMutate.toString

                      succeed(configFileOrFolderToMutate.isFile).flatMap(
                        if (_)
                          if (base.contains(configFileOrFolderToMutateStr))
                            succeed(Seq(configFileOrFolderToMutateStr))
                          else
                            succeed(Seq.empty[String])
                        else
                          succeed(base.filter(_.startsWith(configFileOrFolderToMutateStr)))
                      )
                    }
              } yield result
            else
              copyFilesToTempFolder.flatMap(_ => succeed(Seq("all")))

          runResult <-
            if (filesToMutate.isEmpty)
              ConsoleReporter.filesToMutateIsEmpty
                .flatMap(_ => succeed(ExitCode.success))
            else
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
                  WriteFile(confFile, s"""rules = $ruleName
                                         |Blinky $scalaFixConf""".stripMargin, succeed(confFile))
                }

                toolPath <- runAsync(
                  coursier,
                  Seq(
                    "fetch",
                    s"com.github.rcmartins:${ruleName.toLowerCase}_2.12:${BuildInfo.version}",
                    "-p"
                  ),
                  Map("COURSIER_REPOSITORIES" -> "ivy2Local|sonatype:snapshots|sonatype:releases")
                )(projectRealPath)

                _ <- {
                  val params: Seq[String] =
                    Seq(
                      if (config.options.verbose) "--verbose" else "",
                      if (config.filesToExclude.nonEmpty) s"--exclude=${config.filesToExclude}"
                      else "",
                      s"--tool-classpath=$toolPath",
                      s"--files=${config.filesToMutate}",
                      s"--config=$scalafixConfFile",
                      "--auto-classpath=target"
                    ).filter(_.nonEmpty)

                  runSync("./scalafix", params)(projectRealPath)
                }

                runResult <- TestMutationsBloop.run(projectRealPath, blinkyConf, config.options)
              } yield runResult
        } yield runResult
      }
    } yield inst

}
