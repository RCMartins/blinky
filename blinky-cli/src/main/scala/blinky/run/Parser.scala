package blinky.run

import java.io.File

import ammonite.ops._
import scopt.{OParser, OParserBuilder}

object Parser {
  private val builder: OParserBuilder[MutationsConfig] = OParser.builder[MutationsConfig]
  val parser: OParser[Unit, MutationsConfig] = {
    import builder._
    OParser.sequence(
      programName("blinky"),
      head("blinky", "v0.2.0"),
      help("help")
        .text("prints this usage text"),
      version('v', "version")
        .text("prints blinky version"),
      arg[File]("<blinkyConfFile>")
        .action((confFilePath, _) => {
          MutationsConfig.read(read(Path(confFilePath.getAbsolutePath)))
        })
        .required(),
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
        .text("The project name, used for bloop compile and test commands"),
      opt[String]("projectPath")
        .valueName("<path>")
        .action((projectPath, config) => {
          config.copy(projectPath = projectPath)
        })
        .text("The project directory, can be an absolute or relative path"),
      opt[String]("filesToMutate")
        .valueName("<path>")
        .action((filesToMutate, config) => {
          config.copy(filesToMutate = filesToMutate)
        })
        .text("The relative path to the scala folder or files to mutate"),
      opt[String]("blinkyVersion")
        .valueName("<version>")
        .action((blinkyVersion, config) => {
          config.copy(blinkyVersion = blinkyVersion)
        })
        .text("The Blinky version to use to mutate the code"),
      opt[String]("compileCommand")
        .valueName("<command>")
        .action((compileCommand, config) => {
          config.copy(options = config.options.copy(compileCommand = compileCommand))
        })
        .text("The compile command to be executed by sbt/bloop before the first run"),
      opt[String]("testCommand")
        .valueName("<command>")
        .action((testCommand, config) => {
          config.copy(options = config.options.copy(testCommand = testCommand))
        })
        .text("The test command to be executed by sbt/bloop"),
      opt[Boolean]("verbose")
        .valueName("<bool>")
        .action((verbose, config) => {
          config.copy(options = config.options.copy(verbose = verbose))
        })
        .text("If set, prints out debug information. Defaults to false")
    )
  }
}
