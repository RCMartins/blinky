//package blinky.cli
//
//import better.files.File
//import blinky.run._
//import scopt.{DefaultOParserSetup, OParser, OParserSetup}
//
//object Cli {
//
//  private val setup: DefaultOParserSetup = new DefaultOParserSetup() {}
//
//  def main(args: Array[String]): Unit =
//    parse(args, setup)(File(".")) match {
//      case Some(config) =>
//        val successfulRun = Run.run(config)
//        if (!successfulRun)
//          System.exit(1)
//      case _ =>
//      // arguments are bad, error message will have been displayed by OParser.parse
//    }
//
//  def parse(args: Array[String], setup: OParserSetup)(pwd: File): Option[MutationsConfigValidated] =
//    OParser.parse(Parser.parser, args, Args(), setup).flatMap { args =>
//      val confFileResult: Either[String, File] =
//        args.mainConfFile match {
//          case Some(confFileStr) =>
//            val confFile = File(pwd.path.resolve(confFileStr))
//            if (!confFile.exists)
//              Left(s"""<blinkyConfFile> '$confFile' does not exist.
//                      |blinky --help for usage.""".stripMargin)
//            else
//              Right(confFile)
//          case None =>
//            val confFile = pwd / ".blinky.conf"
//            if (!confFile.exists)
//              Left(s"""Default '$confFile' does not exist.
//                      |blinky --help for usage.""".stripMargin)
//            else
//              Right(confFile)
//        }
//
//      confFileResult.flatMap { confFile =>
//        val initialMutationsConf =
//          MutationsConfig.read(confFile.contentAsString)
//        val config = args.overrides.foldLeft(initialMutationsConf)((conf, over) => over(conf))
//        MutationsConfigValidated.validate(config)(pwd)
//      } match {
//        case Left(errorMessage) =>
//          Console.err.println(errorMessage)
//          None
//        case Right(mutationsConfigValidated) =>
//          Some(mutationsConfigValidated)
//      }
//    }
//}
