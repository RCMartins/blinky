package blinky.run

import blinky.cli.Cli.InterpreterEnvironment
import blinky.run.Instruction._
import blinky.run.external.ExternalCalls
import blinky.run.modules.ExternalModule
import zio.URIO

import scala.annotation.tailrec

object Interpreter {

  def interpreter[A](
      initialProgram: Instruction[A]
  ): URIO[InterpreterEnvironment, A] =
    for {
      externalCalls <- ExternalModule.external
    } yield interpreterFully(externalCalls, initialProgram)

  private def interpreterFully[A](
      externalCalls: ExternalCalls,
      initialProgram: Instruction[A]
  ): A = {
    @tailrec
    def interpreterNext(program: Instruction[A]): A =
      program match {
        case Return(value) =>
          value()
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
        case RunStream(op, args, envArgs, path, next) =>
          val result = externalCalls.runStream(op, args, envArgs, None, path)
          interpreterNext(next(result))
        case RunResultEither(op, args, envArgs, path, next) =>
          val result = externalCalls.runResult(op, args, envArgs, None, path)
          interpreterNext(next(result))
        case RunResultTimeout(op, args, envArgs, timeout, path, next) =>
          val result = externalCalls.runResult(op, args, envArgs, Some(timeout), path)
          interpreterNext(next(resultTimeout(result)))
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
      }

    interpreterNext(initialProgram)
  }

  private def resultTimeout(result: Either[Throwable, String]): Either[Throwable, TimeoutResult] =
    result match {
      case Right(_) =>
        Right(TimeoutResult.Ok)
      case Left(throwable) =>
        println(throwable)
        ???
    }

}
