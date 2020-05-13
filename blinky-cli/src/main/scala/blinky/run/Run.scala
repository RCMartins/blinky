package blinky.run

import ammonite.ops._
import blinky.v0.{BlinkyConfig, Mutator, Mutators}
import metaconfig.{Conf, ConfEncoder, generic}

import scala.util.Try

object Run {
  val path: Path = pwd

  def run(config: MutationsConfig): Unit = {
    val ruleName = "Blinky"
    val projectPath = Try(Path(config.projectPath)).getOrElse(path / RelPath(config.projectPath))

    val (mutatedProjectPath, coursier) = {
      val tempFolder = tmp.dir()
      val cloneProjectPath = tempFolder / 'project
      if (config.options.verbose)
        println(s"Temporary project folder: $tempFolder")

      val filesToCopy =
        %%("git", "ls-files", "--others", "--exclude-standard", "--cached")(projectPath).out.string.trim
          .split("\n")
          .toList
      filesToCopy.foreach { fileToCopyStr =>
        val fileToCopy = RelPath(fileToCopyStr)
        mkdir(cloneProjectPath / fileToCopy / up)
        cp.into(projectPath / fileToCopy, cloneProjectPath / fileToCopy / up)
      }

      %(
        'sbt,
        "set ThisBuild / semanticdbEnabled := true",
        "set ThisBuild / semanticdbVersion := \"4.3.10\"",
        "compile"
      )(cloneProjectPath)

      val coursier =
        if (Try(%%('coursier, "--help")(cloneProjectPath)).isSuccess) {
          "coursier"
        } else {
          %("curl", "-Lo", "coursier", "https://git.io/coursier-cli")(cloneProjectPath)
          %("chmod", "+x", "coursier")(cloneProjectPath)
          "./coursier"
        }

      %(
        coursier,
        "bootstrap",
        "ch.epfl.scala:scalafix-cli_2.12.10:0.9.9",
        "-f",
        "--main",
        "scalafix.cli.Cli",
        "-o",
        "scalafix"
      )(cloneProjectPath)

      (cloneProjectPath, coursier)
    }

    implicit val mutatorEncoder: ConfEncoder[Mutator] =
      (value: Mutator) => Conf.Str(value.name)

    implicit val mutatorsEncoder: ConfEncoder[Mutators] =
      (value: Mutators) => ConfEncoder[List[Mutator]].write(value.mutations)

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
      coursier,
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

    TestMutations.run(mutatedProjectPath, config.options)
  }
}
