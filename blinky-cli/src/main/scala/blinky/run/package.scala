package blinky

import ammonite.ops.Path
import blinky.run.Instruction._

package object run {

  implicit class ConsoleSyntax[+A](self: Instruction[A]) {
    def map[B](f: A => B): Instruction[B] =
      flatMap(a => succeed(f(a)))

    def flatMap[B](
        f: A => Instruction[B]
    ): Instruction[B] =
      self match {
        case Return(value) =>
          f(value())
        case Empty(next) =>
          next.flatMap(f)
        case PrintLine(line, next) =>
          PrintLine(line, next.flatMap(f))
        case PrintErrorLine(line, next) =>
          PrintErrorLine(line, next.flatMap(f))
        case RunSync(op, args, envArgs, path, next) =>
          RunSync(op, args, envArgs, path, next.flatMap(f))
        case RunAsync(op, args, envArgs, path, next) =>
          RunAsync(op, args, envArgs, path, next(_: Either[String, String]).flatMap(f))
        case RunAsyncSuccess(op, args, envArgs, path, next) =>
          RunAsyncSuccess(op, args, envArgs, path, next(_: Boolean).flatMap(f))
        case RunAsyncEither(op, args, envArgs, path, next) =>
          RunAsyncEither(op, args, envArgs, path, next(_: Either[String, String]).flatMap(f))
        case MakeTemporaryDirectory(next) =>
          MakeTemporaryDirectory(next(_: Path).flatMap(f))
        case MakeDirectory(path, next) =>
          MakeDirectory(path, next.flatMap(f))
        case CopyInto(from, to, next) =>
          CopyInto(from, to, next.flatMap(f))
        case CopyResource(resource, destinationPath, next) =>
          CopyResource(resource, destinationPath, next.flatMap(f))
        case WriteFile(path, content, next) =>
          WriteFile(path, content, next.flatMap(f))
        case ReadFile(path, next) =>
          ReadFile(path, next(_: Either[Throwable, String]).flatMap(f))
        case IsFile(path, next) =>
          IsFile(path, next(_: Boolean).flatMap(f))
        case CopyRelativeFiles(filesToCopy, fromPath, toPath, next) =>
          CopyRelativeFiles(filesToCopy, fromPath, toPath, next.flatMap(f))
        case Timeout(runFunction, millis, next) =>
          Timeout(runFunction, millis, next(_: Option[Boolean]).flatMap(f))
      }
  }

}
