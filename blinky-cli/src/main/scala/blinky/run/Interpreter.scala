package blinky.run

import java.nio.file.{Files, Paths}

import blinky.run.Instruction._
import blinky.run.external.ExternalCalls

import scala.annotation.tailrec

object Interpreter {

  def interpreter[A](externalCalls: ExternalCalls, initialProgram: Instruction[A]): A = {

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
          externalCalls.makeDirectory(path)
          interpreterNext(next)
        case RunSync(op, args, envArgs, path, next) =>
          println((op, args, envArgs, path))
          externalCalls.runSync(op, args, envArgs, path)
          interpreterNext(next)
        case RunAsync(op, args, envArgs, path, next) =>
          println((op, args, envArgs, path))
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
      }

    interpreterNext(initialProgram)
  }

}
