package blinky.cli

import better.files.File
import blinky.run.Instruction._
import blinky.run._
import blinky.run.config._
import blinky.run.modules.{CliModule, ExternalModule, ParserModule}
import scopt.OParser
import zio.{ExitCode, URIO, ZEnv, ZIO}

object Cli extends zio.App {

  private type FullEnvironment = ParserModule with ExternalModule with CliModule

  private type ParserEnvironment = ParserModule with CliModule

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    parseAndRun(args).provideLayer {
      ParserModule.live ++ CliModule.live(File(".")) ++ ExternalModule.live
    }

  private def parseAndRun(strArgs: List[String]): URIO[FullEnvironment, ExitCode] =
    for {
      external <- ExternalModule.external
      parseResult <- parse(strArgs)
      instructions <- parseResult match {
        case Left(exitCode) =>
          ZIO.succeed(PrintErrorLine(exitCode, succeed(ExitCode.failure)))
        case Right(configValidated) =>
          Run
            .run(configValidated)
            .catchAll(throwable =>
              ZIO.succeed(PrintErrorLine(throwable.getMessage, succeed(ExitCode.failure)))
            )
      }
    } yield Interpreter.interpreter(external, instructions)

  private[cli] def parse(
      strArgs: List[String]
  ): URIO[ParserEnvironment, Either[String, MutationsConfigValidated]] =
    for {
      parser <- ParserModule.parser
      pwd <- CliModule.pwd
      args <- ZIO.succeed(OParser.parse(Parser.parser, strArgs, Args(), parser))
    } yield args match {
      case None =>
        Left("")
      case Some(Args(mainConfFile, overrides)) =>
        val confFileResult: Either[String, File] =
          mainConfFile match {
            case Some(confFileStr) =>
              val confFile = File(pwd.path.resolve(confFileStr))
              if (!confFile.exists)
                Left(s"""<blinkyConfFile> '$confFile' does not exist.
                        |blinky --help for usage.""".stripMargin)
              else
                Right(confFile)
            case None =>
              val confFile = pwd / ".blinky.conf"
              if (!confFile.exists)
                Left(s"""Default '$confFile' does not exist.
                        |blinky --help for usage.""".stripMargin)
              else
                Right(confFile)
          }

        confFileResult.flatMap { confFile =>
          MutationsConfig.read(confFile.contentAsString) match {
            case Left(confError) =>
              Left(confError.msg)
            case Right(initialMutationsConf) =>
              val config = overrides.foldLeft(initialMutationsConf)((conf, over) => over(conf))
              MutationsConfigValidated.validate(config)(pwd)
          }
        }
    }

}
