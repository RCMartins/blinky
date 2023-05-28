package blinky.cli

import better.files.File
import blinky.run.Instruction._
import blinky.run._
import blinky.run.config._
import blinky.run.modules.{CliModule, ExternalModule, ParserModule}
import scopt.OParser
import zio.{ExitCode, URIO, ZIO, ZIOAppArgs, ZIOAppDefault}

object Cli extends ZIOAppDefault {

  private type FullEnvironment = ParserModule with ExternalModule with CliModule
  private type ParserEnvironment = ParserModule with CliModule
  type InterpreterEnvironment = ExternalModule

  override def run: ZIO[ZIOAppArgs, Nothing, ExitCode] =
    ZIO.serviceWith[ZIOAppArgs](_.getArgs.toList).flatMap { args =>
      parseAndRun(args).provide(
        ParserModule.layer,
        CliModule.layer(File(".")),
        ExternalModule.layer
      )
    }

  private def parseAndRun(strArgs: List[String]): URIO[FullEnvironment, ExitCode] =
    for {
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
      result <- Interpreter.interpreter(instructions)
    } yield result

  private[cli] def parse(
      strArgs: List[String]
  ): URIO[ParserEnvironment, Either[String, MutationsConfigValidated]] =
    for {
      parser <- ZIO.serviceWithZIO[ParserModule](_.parser)
      pwd <- ZIO.serviceWithZIO[CliModule](_.pwd)
      args <- ZIO.succeed(OParser.parse(Parser.parser, strArgs, Args(), parser))
      result <-
        args match {
          case None =>
            ZIO.succeed(Left(""))
          case Some(Args(mainConfFileOpt, overrides)) =>
            for {
              confFileStrOpt <-
                ZIO
                  .attemptBlockingIO(
                    mainConfFileOpt.map(confFileStr => File(pwd.path.resolve(confFileStr)))
                  )
                  .option
              confFileResult =
                confFileStrOpt.flatten match {
                  case Some(confFile) =>
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
              result <-
                confFileResult.map { confFile =>
                  MutationsConfig.read(confFile.contentAsString).flatMap { initialMutationsConf =>
                    val config =
                      overrides.foldLeft(initialMutationsConf)((conf, over) => over(conf))
                    MutationsConfigValidated.validate(config)(pwd)
                  }
                } match {
                  case Left(errorMsg) => ZIO.succeed(Left(errorMsg))
                  case Right(value)   => value.mapError(_.getMessage).either
                }
            } yield result
        }
    } yield result

}
