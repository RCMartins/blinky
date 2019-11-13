package blinky.run

import ammonite.ops._
import blinky.v0.{BlinkyConfig, Mutator, Mutators}
import metaconfig.{Conf, ConfEncoder, generic}

import scala.util.Try

object Run {

  val path: Path = pwd

  def run(confFilePath: Path = pwd / ".mutations.conf"): Unit = {
    run(MutationsConfig.read(read(confFilePath)))
  }

  def run(config: MutationsConfig): Unit = {
    val ruleName = "blinky"
    val projectPath = Try(Path(config.projectPath)).getOrElse(pwd / RelPath(config.projectPath))

    val mutatedProjectPath = {
      val tempFolder = tmp.dir()
      val cloneProjectPath = tempFolder / 'project
      println(tempFolder)

      val filesToCopy =
        %%("git", "ls-files", "--others", "--exclude-standard", "--cached")(projectPath).out.string.trim
          .split("\n")
          .toList
      filesToCopy.foreach { fileToCopyStr =>
        val fileToCopy = RelPath(fileToCopyStr)
        mkdir(cloneProjectPath / fileToCopy / up)
        cp.into(projectPath / fileToCopy, cloneProjectPath / fileToCopy / up)
      }

      %('sbt, "compile")(cloneProjectPath)

      %("curl", "-Lo", "coursier", "https://git.io/coursier-cli")(cloneProjectPath)
      %("chmod", "+x", "coursier")(cloneProjectPath)

      %(
        "./coursier",
        "bootstrap",
        "ch.epfl.scala:scalafix-cli_2.12.10:0.9.7",
        "-f",
        "--main",
        "scalafix.cli.Cli",
        "-o",
        "scalafix"
      )(cloneProjectPath)

      cloneProjectPath
    }

    implicit val mutatorEncoder: ConfEncoder[Mutator] = (value: Mutator) => Conf.Str(value.name)

    implicit val mutatorsEncoder: ConfEncoder[Mutators] = generic.deriveEncoder[Mutators]

    val blinkyConfigEncoder: ConfEncoder[BlinkyConfig] = generic.deriveEncoder[BlinkyConfig]

    val scalafixConfFile = {
      val scalaFixConf =
        blinkyConfigEncoder
          .write(config.conf.copy(projectPath = mutatedProjectPath.toString))
          .show
          .trim

      val tempFolder = tmp.dir()
      write(tempFolder / ".scalafix.conf", s"Blinky $scalaFixConf")
      tempFolder / ".scalafix.conf"
    }

    val fileName = config.filesToMutate

    val semanticDbPath = "target"

    val toolPath = %%(
      "./coursier",
      "fetch",
      s"com.github.rcmartins:${ruleName.toLowerCase}_2.12:${config.blinkyVersion}",
      "-p",
      COURSIER_REPOSITORIES = "ivy2Local|sonatype:snapshots|sonatype:releases"
    )(mutatedProjectPath).out.string.trim

    %(
      "./scalafix",
      "--verbose",
      "--tool-classpath",
      toolPath,
      "--rules",
      ruleName,
      "--files",
      fileName,
      "--config",
      scalafixConfFile,
      "--auto-classpath",
      semanticDbPath
    )(mutatedProjectPath)

    TestMutations.run(mutatedProjectPath, config.testCommand, config.options)
  }

}
