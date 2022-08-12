package blinky.run

import blinky.cli.Cli.InterpreterEnvironment
import blinky.run.Instruction._
import blinky.run.external.ExternalCalls
import blinky.run.modules.ExternalModule
import os.{CommandResult, SubprocessException}
import zio.{URIO, ZIO}

import scala.annotation.tailrec

object Interpreter {

  def interpreter[A](
      initialProgram: Instruction[A]
  ): URIO[InterpreterEnvironment, A] =
    for {
      externalCalls <- ZIO.service[ExternalModule].flatMap(_.external)
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
          val result = externalCalls.runStream(op, args, envArgs, path)
          interpreterNext(next(result))
        case RunResultEither(op, args, envArgs, path, next) =>
          val result = externalCalls.runResult(op, args, envArgs, None, path)
          interpreterNext(next(result))
        case RunResultTimeout(op, args, envArgs, timeout, path, next) =>
          val initialTime = System.currentTimeMillis()
          val result = externalCalls.runResult(op, args, envArgs, Some(timeout), path)
          interpreterNext(next(resultTimeout(result, initialTime, timeout)))
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

  private def resultTimeout(
      result: Either[Throwable, String],
      initialTime: Long,
      timeout: Long
  ): Either[Throwable, TimeoutResult] =
    result match {
      case Right(res) =>
        println(s"ok: $res")
        Right(TimeoutResult.Ok)
      case Left(res @ SubprocessException(CommandResult(_, _))) =>
        println("left")
        val elapsedTime = System.currentTimeMillis() - initialTime
        if (elapsedTime >= timeout)
          Right(TimeoutResult.Timeout)
        else
          Left(res)
      case Left(throwable) =>
        println("throwable")
        Left(throwable)
    }

}
