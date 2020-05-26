package blinky.run

import better.files.File
import blinky.BuildInfo
import com.softwaremill.quicklens._
import scopt.{OParser, OParserBuilder, Read}

object Parser {

  private implicit val readFile: Read[File] = new Read[File] {
    override def arity: Int = 1
    override def reads: String => File = File(_)
  }

  private val builder: OParserBuilder[Args] = OParser.builder[Args]
  val parser: OParser[Unit, Args] = {
    import builder._
    OParser.sequence(
      programName("blinky"),
      head("blinky", s"v${BuildInfo.version}"),
      help("help")
        .text("prints this usage text"),
      version('v', "version")
        .text("prints blinky version"),
      arg[File]("<blinkyConfFile>")
        .action((confFile, args) => args.copy(mainConfFile = Some(confFile)))
        .validate(file =>
          if (file.exists) success
          else failure(s"<blinkyConfFile> '$file' does not exists.")
        )
        .optional()
        .maxOccurs(1),
      opt[String]("projectName")
        .valueName("<path>")
        .action((projectName, config) => {
          config.add(
            _.modifyAll(_.options.compileCommand, _.options.testCommand).setTo(projectName)
          )
        })
        .text("The project name, used for bloop compile and test commands")
        .maxOccurs(1),
      opt[File]("projectPath")
        .valueName("<path>")
        .action((projectPath, config) => config.add(_.copy(projectPath = projectPath)))
        .validate(file =>
          if (file.exists) success
          else failure(s"--projectPath '$file' does not exists.")
        )
        .text("The project directory, can be an absolute or relative path")
        .maxOccurs(1),
      opt[String]("filesToMutate")
        .valueName("<path>")
        .action((filesToMutate, config) => config.add(_.copy(filesToMutate = filesToMutate)))
        .text("The relative path to the scala src folder or files to mutate")
        .maxOccurs(1),
      opt[String]("filesToExclude")
        .valueName("<path>")
        .action((filesToExclude, config) => config.add(_.copy(filesToExclude = filesToExclude)))
        .text("The relative path to the folder or files to exclude from mutation")
        .maxOccurs(1),
      opt[String]("compileCommand")
        .valueName("<cmd>")
        .action((compileCommand, config) => {
          config.add(_.modify(_.options.compileCommand).setTo(compileCommand))
        })
        .text("The compile command to be executed by sbt/bloop before the first run")
        .maxOccurs(1),
      opt[String]("testCommand")
        .valueName("<cmd>")
        .action((testCommand, config) => {
          config.add(_.modify(_.options.testCommand).setTo(testCommand))
        })
        .text("The test command to be executed by sbt/bloop")
        .maxOccurs(1),
      opt[Boolean]("verbose")
        .valueName("<bool>")
        .action((verbose, config) => {
          config.add(_.modify(_.options.verbose).setTo(verbose))
        })
        .text("If set, prints out debug information. Defaults to false")
        .maxOccurs(1),
      opt[Boolean]("onlyMutateDiff")
        .valueName("<bool>")
        .action((onlyMutateDiff, config) => {
          config.add(_.modify(_.options.onlyMutateDiff).setTo(onlyMutateDiff))
        })
        .text("If set, only mutate added and edited files in git diff against the master branch")
        .maxOccurs(1)
    )
  }
}
