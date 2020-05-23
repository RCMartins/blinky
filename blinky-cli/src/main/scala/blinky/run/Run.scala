package blinky.run

import java.nio.file.{Files, Paths}

import ammonite.ops.Shellable._
import ammonite.ops._
import blinky.BuildInfo
import blinky.v0.BlinkyConfig

import scala.util.Try

object Run {
  def run(config: MutationsConfig): Unit = {
    val ruleName = "Blinky"
    val originalProjectRoot = pwd
    val originalProjectRelPath = RelPath(config.projectPath)
    val originalProjectPath = originalProjectRoot / originalProjectRelPath

    val cloneProjectTempFolder = tmp.dir()

    val (mutatedProjectPath, filesToMutate, coursier) = {
      if (config.options.verbose)
        println(s"Temporary project folder: $cloneProjectTempFolder")

      val gitFolder: Path =
        Path(%%("git", "rev-parse", "--show-toplevel")(originalProjectRoot).out.string.trim)

      val cloneProjectBaseFolder: Path =
        cloneProjectTempFolder / gitFolder.baseName
      mkdir(cloneProjectBaseFolder)

      val projectRealRelPath: RelPath =
        originalProjectPath.relativeTo(gitFolder)

      // Copy only the files tracked by git into our temporary folder
      val filesToCopy: Seq[RelPath] =
        %%("git", "ls-files", "--others", "--exclude-standard", "--cached")(
          originalProjectPath
        ).out.lines.map(RelPath(_))

      filesToCopy.foreach { fileToCopy =>
        mkdir(cloneProjectBaseFolder / projectRealRelPath / fileToCopy / up)
        cp.into(
          originalProjectRoot / fileToCopy,
          cloneProjectBaseFolder / projectRealRelPath / fileToCopy / up
        )
      }

      val projectRealPath =
        cloneProjectBaseFolder / projectRealRelPath

      // Setup files to mutate ('scalafix --diff' does not work like I want...)
      val filesToMutate: Seq[String] =
        if (config.options.onlyMutateDiff) {
          // maybe copy the .git folder so it can be used by TestMutations, etc?
          //cp(gitFolder / ".git", cloneProjectBaseFolder / ".git")

          val masterHash = %%("git", "rev-parse", "master")(gitFolder).out.string.trim
          val diffLines =
            %%.git("--no-pager", 'diff, "--name-only", masterHash)(gitFolder).out.lines

          val base: Seq[String] =
            diffLines
              .map(file => cloneProjectBaseFolder / RelPath(file))
              .filter(file => file.ext == "scala" || file.ext == "sbt")
              .map(_.toString)

          // This part is just an optimization of 'base'
          val configFileOrFolderToMutate: Path =
            Try(Path(config.filesToMutate))
              .getOrElse(projectRealPath / RelPath(config.filesToMutate))

          val configFileOrFolderToMutateStr =
            configFileOrFolderToMutate.toString

          if (configFileOrFolderToMutate.isFile)
            if (base.contains(configFileOrFolderToMutateStr))
              Seq(configFileOrFolderToMutateStr)
            else
              Seq.empty
          else
            base.filter(_.startsWith(configFileOrFolderToMutateStr))
        } else {
          Seq("all")
        }

      // Setup semanticdb files with sbt compile.
      // (there should probably be a better way to do this...)
      %(
        'sbt,
        "set ThisBuild / semanticdbEnabled := true",
        "set ThisBuild / semanticdbVersion := \"4.3.12\"",
        "compile"
      )(projectRealPath)

      // Setup coursier
      val coursier =
        if (Try(%%('coursier, "--help")(projectRealPath)).isSuccess)
          "coursier"
        else if (Try(%%('cs, "--help")(projectRealPath)).isSuccess)
          "cs"
        else
          Setup.setupCoursier(projectRealPath)

      // Setup scalafix
      Files.copy(
        getClass.getResource(s"/scalafix").openStream,
        Paths.get(projectRealPath.toString, "scalafix")
      )
      %("chmod", "+x", "scalafix")(projectRealPath)

      (projectRealPath, filesToMutate, coursier)
    }

    // Setup BlinkyConfig object
    val blinkyConf: BlinkyConfig =
      BlinkyConfig(
        mutantsOutputFile = (mutatedProjectPath / "mutants.blinky").toString,
        filesToMutate = filesToMutate,
        enabledMutators = config.mutators.enabled,
        disabledMutators = config.mutators.disabled
      )

    // Setup our .blinky.scalafix.conf file to be used by Blinky rule
    val scalafixConfFile = {
      val scalaFixConf =
        SimpleBlinkyConfig.blinkyConfigEncoder.write(blinkyConf).show.trim

      val confFile = cloneProjectTempFolder / ".scalafix.conf"
      write(confFile, s"""rules = $ruleName
                         |Blinky $scalaFixConf""".stripMargin)
      confFile
    }

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
        if (config.filesToExclude.nonEmpty) s"--exclude=${config.filesToExclude}" else "",
        s"--tool-classpath=$toolPath",
        s"--files=${config.filesToMutate}",
        s"--config=$scalafixConfFile",
        s"--auto-classpath=$semanticDbPath"
      ).filter(_.nonEmpty)

    %.applyDynamic("./scalafix")(params.map(StringShellable): _*)(mutatedProjectPath)

    TestMutationsBloop.run(mutatedProjectPath, blinkyConf, config.options)
  }
}
