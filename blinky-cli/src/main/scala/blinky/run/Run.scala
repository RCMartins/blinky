package blinky.run

import ammonite.ops.Shellable._
import ammonite.ops._
import blinky.BuildInfo
import blinky.v0.BlinkyConfig

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
        %%("git", "ls-files", "--others", "--exclude-standard", "--cached")(
          projectPath
        ).out.string.trim
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
        if (Try(%%('coursier, "--help")(cloneProjectPath)).isSuccess)
          "coursier"
        else if (Try(%%('cs, "--help")(cloneProjectPath)).isSuccess)
          "cs"
        else
          Setup.setupCoursier(cloneProjectPath)

      %(
        coursier,
        "bootstrap",
        "ch.epfl.scala:scalafix-cli_2.12.11:0.9.15",
        "-f",
        "--main",
        "scalafix.cli.Cli",
        "-o",
        "scalafix"
      )(cloneProjectPath)

      (cloneProjectPath, coursier)
    }

    val scalafixConfFile = {
      val scalaFixConf =
        BlinkyConfig.blinkyConfigEncoder
          .write(config.conf.copy(projectPath = mutatedProjectPath.toString))
          .show
          .trim

      val tempFolder = tmp.dir()
      val confFile = tempFolder / ".blinky.scalafix.conf"
      write(confFile, s"Blinky $scalaFixConf")
      confFile
    }

    val fileName = config.filesToMutate

    val semanticDbPath = "target"

    val toolPath = %%(
      coursier,
      "fetch",
      s"com.github.rcmartins:${ruleName.toLowerCase}_2.12:${BuildInfo.version}",
      "-p",
      COURSIER_REPOSITORIES = "ivy2Local|sonatype:snapshots|sonatype:releases"
    )(mutatedProjectPath).out.string.trim

    val params: Seq[String] =
      Seq(
        "--verbose",
        "--tool-classpath",
        toolPath,
        "--rules",
        ruleName,
        "--files",
        fileName,
        "--config",
        scalafixConfFile.toString,
        "--auto-classpath",
        semanticDbPath
      ) ++
        (if (config.filesToExclude.nonEmpty) Seq("--exclude", config.filesToExclude)
         else Seq.empty) ++
        (if (config.options.onlyMutateDiff) Seq("--diff") else Seq.empty)

    %.applyDynamic("./scalafix")(params.map(StringShellable): _*)(mutatedProjectPath)

    TestMutations.run(mutatedProjectPath, config.options)
  }
}
