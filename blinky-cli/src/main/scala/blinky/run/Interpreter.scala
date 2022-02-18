package blinky.run

import blinky.cli.Cli.InterpreterEnvironment
import blinky.run.Instruction._
import blinky.run.external.ExternalCalls
import blinky.run.modules.ExternalModule
import zio.URIO
import zio.duration.durationLong

import java.nio.file.{Files, Paths}
import scala.annotation.tailrec

object Interpreter {

  def interpreter[A](
      initialProgram: Instruction[A]
  ): URIO[InterpreterEnvironment, A] =
    for {
      externalCalls <- ExternalModule.external
      result <- interpreterFully(externalCalls, initialProgram) match {
        case Left(Timeout(runFunction, millis, next)) =>
          interpreter(runFunction).disconnect.timeout(millis.millis).flatMap { instruction =>
            interpreter(next(instruction))
          }
        case Right(value) =>
          URIO.succeed(value)
      }
    } yield result

  private def interpreterFully[A](
      externalCalls: ExternalCalls,
      initialProgram: Instruction[A]
  ): Either[Timeout[A], A] = {
    @tailrec
    def interpreterNext(program: Instruction[A]): Either[Timeout[A], A] =
      program match {
        case Return(value) =>
          Right(value())
        case Empty(next) =>
          interpreterNext(next)
        case PrintLine(line, next) =>
          Console.out.println(line)
          interpreterNext(next)
        case PrintErrorLine(line, next) =>
          Console.err.println(line)
          interpreterNext(next)
        case MakeTemporaryDirectory(next) =>
          val path = externalCalls.makeTemporaryDirectory()
          interpreterNext(next(path))
        case MakeDirectory(path, next) =>
          externalCalls.makeDirectory(path)
          interpreterNext(next)
        case RunSync(op, args, envArgs, path, next) =>
          externalCalls.runSync(op, args, envArgs, path)
          interpreterNext(next)
        case RunAsync(op, args, envArgs, path, next) =>
          val result = externalCalls.runAsync(op, args, envArgs, path)
          interpreterNext(next(result))
        case RunAsyncSuccess(op, args, envArgs, path, next) =>
          val result = externalCalls.runAsync(op, args, envArgs, path)
          interpreterNext(next(result.isRight))
        case RunAsyncEither(op, args, envArgs, path, next) =>
          externalCalls.runAsync(op, args, envArgs, path) match {
            case Left(value) =>
              interpreterNext(next(Left(value)))
            case Right(value) =>
              interpreterNext(next(Right(value)))
          }
        case CopyInto(from, to, next) =>
          externalCalls.copyInto(from, to)
          interpreterNext(next)
        case CopyResource(resource, destinationPath, next) =>
          Files.copy(
            getClass.getResource(resource).openStream,
            Paths.get(destinationPath.toString)
          )
          interpreterNext(next)
        case WriteFile(path, content, next) =>
          externalCalls.writeFile(path, content)
          interpreterNext(next)
        case ReadFile(path, next) =>
          val content = externalCalls.readFile(path)
          interpreterNext(next(content))
        case IsFile(path, next) =>
          val isFile = externalCalls.isFile(path)
          interpreterNext(next(isFile))
        case CopyRelativeFiles(filesToCopy, fromPath, toPath, next) =>
          val result = externalCalls.copyRelativeFiles(filesToCopy, fromPath, toPath)
          interpreterNext(next(result))
        case LsFiles(basePath, next) =>
          val result = externalCalls.lsFiles(basePath)
          interpreterNext(next(result))
        case timeout @ Timeout(_, _, _) =>
          Left(timeout)
      }

    interpreterNext(initialProgram)
  }

}
