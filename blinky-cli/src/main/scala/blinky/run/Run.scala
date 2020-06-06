//package blinky.run
//
//import java.nio.file.{Files, Paths}
//
//import ammonite.ops.{Path, RelPath, cp, pwd, statFileData, up}
//import blinky.BuildInfo
//import blinky.run.ExternalCalls._
//import blinky.v0.BlinkyConfig
//
//import scala.util.Try
//
//object Run {
//  def run(config: MutationsConfigValidated): Boolean = {
//    val ruleName = "Blinky"
//    val originalProjectRoot = pwd
//    val originalProjectRelPath =
//      Try(Path(config.projectPath.pathAsString).relativeTo(originalProjectRoot))
//        .getOrElse(RelPath(config.projectPath.pathAsString))
//    val originalProjectPath = originalProjectRoot / originalProjectRelPath
//
//    val cloneProjectTempFolder = makeTemporaryFolder()
//    if (config.options.verbose)
//      println(s"Temporary project folder: $cloneProjectTempFolder")
//
//    val gitFolder: Path =
//      Path(
//        runAsync("git", Seq("rev-parse", "--show-toplevel"))(originalProjectRoot).out.string.trim
//      )
//
//    val cloneProjectBaseFolder: Path = cloneProjectTempFolder / gitFolder.baseName
//    makeDirectory(cloneProjectBaseFolder)
//    val projectRealRelPath: RelPath = originalProjectPath.relativeTo(gitFolder)
//    val projectRealPath = cloneProjectBaseFolder / projectRealRelPath
//
//    def copyFilesToTempFolder(): Unit = {
//      // Copy only the files tracked by git into our temporary folder
//      val filesToCopy: Seq[RelPath] =
//        runAsync(
//          "git",
//          Seq("ls-files", "--others", "--exclude-standard", "--cached")
//        )(originalProjectPath).out.lines.map(RelPath(_))
//
//      filesToCopy.foreach { fileToCopy =>
//        makeDirectory(projectRealPath / fileToCopy / up)
//        cp.into(
//          originalProjectRoot / fileToCopy,
//          projectRealPath / fileToCopy / up
//        )
//      }
//    }
//
//    // Setup files to mutate ('scalafix --diff' does not work like I want...)
//    val filesToMutate: Seq[String] =
//      if (config.options.onlyMutateDiff) {
//        // maybe copy the .git folder so it can be used by TestMutations, etc?
//        //cp(gitFolder / ".git", cloneProjectBaseFolder / ".git")
//
//        val masterHash = runAsync("git", Seq("rev-parse", "master"))(gitFolder).out.string.trim
//        val diffLines =
//          runAsync("git", Seq("--no-pager", "diff", "--name-only", masterHash))(gitFolder).out.lines
//
//        val base: Seq[String] =
//          diffLines
//            .map(file => cloneProjectBaseFolder / RelPath(file))
//            .filter(file => file.ext == "scala" || file.ext == "sbt")
//            .map(_.toString)
//
//        if (base.isEmpty)
//          base
//        else {
//          copyFilesToTempFolder()
//
//          // This part is just an optimization of 'base'
//          val configFileOrFolderToMutate: Path =
//            Try(Path(config.filesToMutate))
//              .getOrElse(projectRealPath / RelPath(config.filesToMutate))
//
//          val configFileOrFolderToMutateStr =
//            configFileOrFolderToMutate.toString
//
//          if (configFileOrFolderToMutate.isFile)
//            if (base.contains(configFileOrFolderToMutateStr))
//              Seq(configFileOrFolderToMutateStr)
//            else
//              Seq.empty
//          else
//            base.filter(_.startsWith(configFileOrFolderToMutateStr))
//        }
//      } else {
//        copyFilesToTempFolder()
//        Seq("all")
//      }
//
//    if (filesToMutate.isEmpty) {
//      ConsoleReporter.filesToMutateIsEmpty()
//      true
//    } else {
//
//      // Setup coursier
//      val coursier = Setup.setupCoursier(projectRealPath)
//
//      {
//        Setup.sbtCompileWithSemanticDB(projectRealPath)
//
//        // Setup scalafix
//        Files.copy(
//          getClass.getResource(s"/scalafix").openStream,
//          Paths.get(projectRealPath.toString, "scalafix")
//        )
//        runSync("chmod", Seq("+x", "scalafix"))(projectRealPath)
//      }
//
//      // Setup BlinkyConfig object
//      val blinkyConf: BlinkyConfig =
//        BlinkyConfig(
//          mutantsOutputFile = (projectRealPath / "blinky.mutants").toString,
//          filesToMutate = filesToMutate,
//          enabledMutators = config.mutators.enabled,
//          disabledMutators = config.mutators.disabled
//        )
//
//      // Setup our .blinky.scalafix.conf file to be used by Blinky rule
//      val scalafixConfFile = {
//        val scalaFixConf =
//          SimpleBlinkyConfig.blinkyConfigEncoder.write(blinkyConf).show.trim
//
//        val confFile = cloneProjectTempFolder / ".scalafix.conf"
//        writeFile(confFile, s"""rules = $ruleName
//                               |Blinky $scalaFixConf""".stripMargin)
//        confFile
//      }
//
//      val semanticDbPath = "target"
//
//      val toolPath =
//        runAsync(
//          coursier,
//          Seq(
//            "fetch",
//            s"com.github.rcmartins:${ruleName.toLowerCase}_2.12:${BuildInfo.version}",
//            "-p"
//          ),
//          Map("COURSIER_REPOSITORIES" -> "ivy2Local|sonatype:snapshots|sonatype:releases")
//        )(projectRealPath).out.string.trim
//
//      val params: Seq[String] =
//        Seq(
//          if (config.options.verbose) "--verbose" else "",
//          if (config.filesToExclude.nonEmpty) s"--exclude=${config.filesToExclude}" else "",
//          s"--tool-classpath=$toolPath",
//          s"--files=${config.filesToMutate}",
//          s"--config=$scalafixConfFile",
//          s"--auto-classpath=$semanticDbPath"
//        ).filter(_.nonEmpty)
//
//      runSync("./scalafix", params)(projectRealPath)
//
//      TestMutationsBloop.run(projectRealPath, blinkyConf, config.options)
//    }
//  }
//}
