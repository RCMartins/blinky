package blinky.cli

import better.files.File
import blinky.run._
import scopt.{DefaultOParserSetup, OParser, OParserSetup}

object Cli {

  private val setup: DefaultOParserSetup = new DefaultOParserSetup() {}

  def main(args: Array[String]): Unit = {
    parse(args, setup) match {
      case Some(config) =>
        Run.run(config)
      case _ =>
      // arguments are bad, error message will have been displayed by OParser.parse
    }
  }

  def parse(args: Array[String], setup: OParserSetup): Option[MutationsConfigValidated] =
    OParser.parse(Parser.parser, args, Args(), setup).flatMap { args =>
      val initialMutationsConf =
        MutationsConfig.read(args.mainConfFile.getOrElse(File(".blinky.conf")).contentAsString)
      val config = args.overrides.foldLeft(initialMutationsConf)((conf, over) => over(conf))
      MutationsConfigValidated.validate(config) match {
        case Left(errorMessage) =>
          Console.err.println(errorMessage)
          None
        case Right(mutationsConfigValidated) =>
          Some(mutationsConfigValidated)
      }
    }
}
