package blinky.cli

import better.files.File
import blinky.run.Instruction._
import blinky.run._
import blinky.run.config._
import blinky.run.modules.{CliModule, ExternalModule, ParserModule}
import scopt.OParser
import zio.{ExitCode, URIO, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

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
        case Left(errorMessage) =>
          ZIO.succeed(PrintErrorLine(errorMessage, succeed(ExitCode.failure)))
        case Right(configValidated) =>
          val runner = configValidated.options.testRunner match {
            case TestRunnerType.SBT   => RunMutationsSBT
            case TestRunnerType.Bloop => RunMutationsBloop
          }
          ZIO
            .serviceWith[Run](_.run(configValidated))
            .provideSome[CliModule](
              ZLayer.succeed(runner) >+>
                PrettyDiff.live >>>
                RunMutations.live >>>
                Run.live
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
