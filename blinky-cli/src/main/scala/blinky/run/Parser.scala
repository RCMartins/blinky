package blinky.run

import blinky.BuildInfo
import better.files.File
import scopt.{OParser, OParserBuilder, Read}

object Parser {

  private implicit val readFile: Read[File] = new Read[File] {
    override def arity: Int = 1
    override def reads: String => File = File(_)
  }

  private val builder: OParserBuilder[MutationsConfig] = OParser.builder[MutationsConfig]
  val parser: OParser[Unit, MutationsConfig] = {
    import builder._
    OParser.sequence(
      programName("blinky"),
      head("blinky", s"v${BuildInfo.version}"),
      help("help")
        .text("prints this usage text"),
      version('v', "version")
        .text("prints blinky version"),
      arg[File]("<blinkyConfFile>")
        .action((confFile, _) => {
          MutationsConfig.read(confFile.contentAsString)
        })
        .optional()
        .maxOccurs(1),
      opt[String]("projectName")
        .valueName("<path>")
        .action((projectName, config) => {
          config.copy(
            options = config.options.copy(
              compileCommand = projectName,
              testCommand = projectName
            )
          )
        })
        .text("The project name, used for bloop compile and test commands")
        .maxOccurs(1),
      opt[String]("projectPath")
        .valueName("<path>")
        .action((projectPath, config) => {
          config.copy(projectPath = projectPath)
        })
        .text("The project directory, can be an absolute or relative path")
        .maxOccurs(1),
      opt[String]("filesToMutate")
        .valueName("<path>")
        .action((filesToMutate, config) => {
          config.copy(filesToMutate = filesToMutate)
        })
        .text("The relative path to the scala src folder or files to mutate")
        .maxOccurs(1),
      opt[String]("filesToExclude")
        .valueName("<path>")
        .action((filesToExclude, config) => {
          config.copy(filesToExclude = filesToExclude)
        })
        .text("The relative path to the folder or files to exclude from mutation")
        .maxOccurs(1),
      opt[String]("compileCommand")
        .valueName("<cmd>")
        .action((compileCommand, config) => {
          config.copy(options = config.options.copy(compileCommand = compileCommand))
        })
        .text("The compile command to be executed by sbt/bloop before the first run")
        .maxOccurs(1),
      opt[String]("testCommand")
        .valueName("<cmd>")
        .action((testCommand, config) => {
          config.copy(options = config.options.copy(testCommand = testCommand))
        })
        .text("The test command to be executed by sbt/bloop")
        .maxOccurs(1),
      opt[Boolean]("verbose")
        .valueName("<bool>")
        .action((verbose, config) => {
          config.copy(options = config.options.copy(verbose = verbose))
        })
        .text("If set, prints out debug information. Defaults to false")
        .maxOccurs(1)
    )
  }
}
