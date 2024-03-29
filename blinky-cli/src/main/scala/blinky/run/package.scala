package blinky

import blinky.run.Instruction._
import os.Path

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
        case RunStream(op, args, envArgs, path, next) =>
          RunStream(op, args, envArgs, path, next(_: Either[Throwable, Unit]).flatMap(f))
        case RunResultEither(op, args, envArgs, path, next) =>
          RunResultEither(
            op,
            args,
            envArgs,
            path,
            next(_: Either[Throwable, String]).flatMap(f)
          )
        case RunResultTimeout(op, args, envArgs, timeout, path, next) =>
          RunResultTimeout(
            op,
            args,
            envArgs,
            timeout,
            path,
            next(_: Either[Throwable, TimeoutResult]).flatMap(f)
          )
        case MakeTemporaryDirectory(next) =>
          MakeTemporaryDirectory(next(_: Either[Throwable, Path]).flatMap(f))
        case MakeDirectory(path, next) =>
          MakeDirectory(path, next(_: Either[Throwable, Unit]).flatMap(f))
        case CopyInto(from, to, next) =>
          CopyInto(from, to, next(_: Either[Throwable, Unit]).flatMap(f))
        case CopyResource(resource, destinationPath, next) =>
          CopyResource(resource, destinationPath, next(_: Either[Throwable, Unit]).flatMap(f))
        case WriteFile(path, content, next) =>
          WriteFile(path, content, next(_: Either[Throwable, Unit]).flatMap(f))
        case ReadFile(path, next) =>
          ReadFile(path, next(_: Either[Throwable, String]).flatMap(f))
        case IsFile(path, next) =>
          IsFile(path, next(_: Boolean).flatMap(f))
        case CopyRelativeFiles(filesToCopy, fromPath, toPath, next) =>
          CopyRelativeFiles(
            filesToCopy,
            fromPath,
            toPath,
            next(_: Either[Throwable, Unit]).flatMap(f)
          )
        case LsFiles(basePath, next) =>
          LsFiles(basePath, next(_: Either[Throwable, Seq[String]]).flatMap(f))
      }

    /** A variant of `flatMap` that ignores the value produced by this instruction.
      */
    final def *>[B](that: => Instruction[B]): Instruction[B] =
      self.flatMap(_ => that)

  }

}
