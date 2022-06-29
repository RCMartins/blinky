package blinky.run

import blinky.cli.Cli.InterpreterEnvironment
import blinky.run.Instruction._
import blinky.run.external.ExternalCalls
import blinky.run.modules.ExternalModule
import zio.URIO
import zio.duration.durationLong

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
          val result = externalCalls.makeDirectory(path)
          interpreterNext(next(result))
        case RunStream(op, args, envArgs, timeout, path, next) =>
          val result = externalCalls.runStream(op, args, envArgs, timeout, path)
          interpreterNext(next(result))
        case RunResultEither(op, args, envArgs, timeout, path, next) =>
          val result = externalCalls.runResult(op, args, envArgs, timeout, path)
          interpreterNext(next(result))
        case RunResultSuccess(op, args, envArgs, timeout, path, next) =>
          val result = externalCalls.runResult(op, args, envArgs, timeout, path)
          interpreterNext(next(result.isRight))
        case CopyInto(from, to, next) =>
          val result = externalCalls.copyInto(from, to)
          interpreterNext(next(result))
        case CopyResource(resource, destinationPath, next) =>
          val result = externalCalls.copyResource(resource, destinationPath)
          interpreterNext(next(result))
        case WriteFile(path, content, next) =>
          val result = externalCalls.writeFile(path, content)
          interpreterNext(next(result))
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
          val result = externalCalls.listFiles(basePath)
          interpreterNext(next(result))
        case timeout @ Timeout(_, _, _) =>
          Left(timeout)
      }

    interpreterNext(initialProgram)
  }

}
