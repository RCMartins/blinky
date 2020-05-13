package blinky.cli

import blinky.run.{MutationsConfig, Parser, Run}
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

  def parse(args: Array[String], setup: OParserSetup): Option[MutationsConfig] =
    OParser.parse(Parser.parser, args, MutationsConfig.default, setup)
}
