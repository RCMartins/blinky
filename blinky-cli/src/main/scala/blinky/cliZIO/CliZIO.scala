package blinky.cliZIO

import ammonite.ops.Path
import better.files.File
import blinky.runZIO.Instruction._
import blinky.runZIO.config._
import blinky.runZIO.modules.{CliModule, ExternalModule, ParserModule}
import blinky.runZIO._
import scopt.OParser
import zio.{ExitCode, URIO, ZEnv, ZIO}

object CliZIO extends zio.App {

  private type ParserEnvironment = ParserModule with CliModule

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    parseAndRun(args).provide {
      new ParserModule.Live with ExternalModule.Live with CliModule.Live {
        override val pwdLive: File = File(".")
      }
    }

  private def parseAndRun(strArgs: List[String]): URIO[FullEnvironment, ExitCode] =
    parse(strArgs).map { instructions =>
      Interpreter.interpreter(instructions) match {
        case Left(exitCode) =>
          exitCode
        case Right(configValidated) =>
          Interpreter.interpreter(Run.run(configValidated))
      }
    }

  private[cliZIO] def parse(
      strArgs: List[String]
  ): URIO[ParserEnvironment, Instruction[Either[ExitCode, MutationsConfigValidated], Path]] =
    for {
      env <- ZIO.environment[ParserEnvironment]
      parser <- env.parserModule.parser
      pwd <- env.cliModule.pwd
      args <- ZIO.succeed(OParser.parse(Parser.parser, strArgs, Args(), parser))
    } yield args match {
      case None =>
        Result(Left(ExitCode.failure))
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
          val initialMutationsConf =
            MutationsConfig.read(confFile.contentAsString)
          val config = overrides.foldLeft(initialMutationsConf)((conf, over) => over(conf))
          MutationsConfigValidated.validate(config)(pwd)
        } match {
          case Left(errorMessage) =>
            PrintErrorLine(
              errorMessage,
              Result(Left(ExitCode.failure))
            )
          case Right(mutationsConfigValidated) =>
            Result(Right(mutationsConfigValidated))
        }
    }

}
