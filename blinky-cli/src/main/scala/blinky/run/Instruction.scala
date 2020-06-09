package blinky.run

import ammonite.ops.Path

sealed trait Instruction[+A]

object Instruction {

  final case class Return[A](value: () => A) extends Instruction[A]

  final case class Empty[A](next: Instruction[A]) extends Instruction[A]

  final case class PrintLine[A](line: String, next: Instruction[A]) extends Instruction[A]

  final case class PrintErrorLine[A](line: String, next: Instruction[A]) extends Instruction[A]

  final case class RunSync[A](
      op: String,
      args: Seq[String],
      path: Path,
      next: Instruction[A]
  ) extends Instruction[A]

  final case class RunAsync[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      next: String => Instruction[A]
  ) extends Instruction[A]

  final case class RunAsyncSuccess[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      next: Boolean => Instruction[A]
  ) extends Instruction[A]

  final case class RunAsyncEither[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      next: Either[String, String] => Instruction[A]
  ) extends Instruction[A]

  final case class MakeTemporaryDirectory[A](next: Path => Instruction[A]) extends Instruction[A]

  final case class MakeDirectory[A](path: Path, next: Instruction[A]) extends Instruction[A]

  final case class CopyInto[A](from: Path, to: Path, next: Instruction[A]) extends Instruction[A]

  final case class CopyResource[A](resource: String, destinationPath: Path, next: Instruction[A])
      extends Instruction[A]

  final case class WriteFile[A](path: Path, content: String, next: Instruction[A])
      extends Instruction[A]

  final case class ReadFile[A](path: Path, next: String => Instruction[A]) extends Instruction[A]

  final case class IsFile[A](path: Path, next: Boolean => Instruction[A]) extends Instruction[A]

  def succeed[A](value: => A): Return[A] =
    Return(() => value)

  def conditional(cond: Boolean)(value: Instruction[Unit]): Instruction[Unit] =
    if (cond) value else succeed(())

  def printLine(line: String): PrintLine[Unit] =
    PrintLine(line, succeed(()))

  def printErrorLine(line: String): PrintErrorLine[Unit] =
    PrintErrorLine(line, succeed(()))

  def runSync(op: String, args: Seq[String])(path: Path): RunSync[Unit] =
    RunSync(op, args, path, succeed(()))

  def runAsync(op: String, args: Seq[String], envArgs: Map[String, String] = Map.empty)(
      path: Path
  ): RunAsync[String] =
    RunAsync(op, args, envArgs, path, succeed(_: String))

  def runAsyncSuccess(op: String, args: Seq[String], envArgs: Map[String, String] = Map.empty)(
      path: Path
  ): RunAsyncSuccess[Boolean] =
    RunAsyncSuccess(op, args, envArgs, path, succeed(_: Boolean))

  def runAsyncEither(op: String, args: Seq[String], envArgs: Map[String, String] = Map.empty)(
      path: Path
  ): RunAsyncEither[Either[String, String]] =
    RunAsyncEither(op, args, envArgs, path, succeed(_: Either[String, String]))

  def makeTemporaryFolder: MakeTemporaryDirectory[Path] =
    MakeTemporaryDirectory(path => succeed(path))

  def makeDirectory(path: Path): MakeDirectory[Unit] =
    MakeDirectory(path, succeed(()))

  def copyInto(from: Path, to: Path): CopyInto[Unit] =
    CopyInto(from, to, succeed(()))

  def readFile(path: Path): ReadFile[String] =
    ReadFile(path, content => succeed(content))
}
