package blinky.run

import os.{Path, RelPath}

sealed trait Instruction[+A]

object Instruction {

  final case class Return[A](value: () => A) extends Instruction[A]

  final case class Empty[A](next: Instruction[A]) extends Instruction[A]

  final case class PrintLine[A](line: String, next: Instruction[A]) extends Instruction[A]

  final case class PrintErrorLine[A](line: String, next: Instruction[A]) extends Instruction[A]

  final case class RunStream[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      next: Either[Throwable, Unit] => Instruction[A]
  ) extends Instruction[A]

  final case class RunResultTimeout[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      timeout: Long,
      path: Path,
      next: Either[Throwable, TimeoutResult] => Instruction[A]
  ) extends Instruction[A]

  final case class RunResultEither[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      next: Either[Throwable, String] => Instruction[A]
  ) extends Instruction[A]

  final case class MakeTemporaryDirectory[A](next: Either[Throwable, Path] => Instruction[A])
      extends Instruction[A]

  final case class MakeDirectory[A](path: Path, next: Either[Throwable, Unit] => Instruction[A])
      extends Instruction[A]

  final case class CopyInto[A](
      from: Path,
      to: Path,
      next: Either[Throwable, Unit] => Instruction[A]
  ) extends Instruction[A]

  final case class CopyResource[A](
      resource: String,
      destinationPath: Path,
      next: Either[Throwable, Unit] => Instruction[A]
  ) extends Instruction[A]

  final case class WriteFile[A](
      path: Path,
      content: String,
      next: Either[Throwable, Unit] => Instruction[A]
  ) extends Instruction[A]

  final case class ReadFile[A](path: Path, next: Either[Throwable, String] => Instruction[A])
      extends Instruction[A]

  final case class IsFile[A](path: Path, next: Boolean => Instruction[A]) extends Instruction[A]

  final case class CopyRelativeFiles[A](
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path,
      next: Either[Throwable, Unit] => Instruction[A]
  ) extends Instruction[A]

  final case class LsFiles[A](
      basePath: Path,
      next: Either[Throwable, Seq[String]] => Instruction[A]
  ) extends Instruction[A]

  def succeed[A](value: => A): Return[A] =
    Return(() => value)

  val empty: Instruction[Unit] = succeed(())

  def when(cond: Boolean)(value: Instruction[Unit]): Instruction[Unit] =
    if (cond) value else empty

  def printLine(line: String): PrintLine[Unit] =
    PrintLine(line, empty)

  def printErrorLine(line: String): PrintErrorLine[Unit] =
    PrintErrorLine(line, empty)

  def runStream(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty,
      path: Path
  ): RunStream[Either[Throwable, Unit]] =
    RunStream(op, args, envArgs, path, succeed(_: Either[Throwable, Unit]))

  def runResultEither(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty,
      path: Path
  ): RunResultEither[Either[Throwable, String]] =
    RunResultEither(op, args, envArgs, path, succeed(_: Either[Throwable, String]))

  def runBashTimeout(
      arg: String,
      envArgs: Map[String, String],
      timeout: Long,
      path: Path
  ): RunResultTimeout[Either[Throwable, TimeoutResult]] =
    RunResultTimeout(
      "bash",
      Seq("-c", arg),
      envArgs,
      timeout,
      path,
      succeed(_: Either[Throwable, TimeoutResult])
    )

  def runBashEither(
      arg: String,
      envArgs: Map[String, String] = Map.empty,
      path: Path
  ): RunResultEither[Either[Throwable, String]] =
    RunResultEither(
      "bash",
      Seq("-c", arg),
      envArgs,
      path,
      succeed(_: Either[Throwable, String])
    )

  def makeTemporaryFolder: MakeTemporaryDirectory[Either[Throwable, Path]] =
    MakeTemporaryDirectory(path => succeed(path))

  def makeDirectory(path: Path): MakeDirectory[Either[Throwable, Unit]] =
    MakeDirectory(path, succeed(_: Either[Throwable, Unit]))

  def copyInto(from: Path, to: Path): CopyInto[Either[Throwable, Unit]] =
    CopyInto(from, to, succeed(_: Either[Throwable, Unit]))

  def readFile(path: Path): ReadFile[Either[Throwable, String]] =
    ReadFile(path, succeed(_: Either[Throwable, String]))

  def copyRelativeFiles(
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path
  ): CopyRelativeFiles[Either[Throwable, Unit]] =
    CopyRelativeFiles(filesToCopy, fromPath, toPath, succeed(_: Either[Throwable, Unit]))

  def lsFiles(basePath: Path): LsFiles[Either[Throwable, Seq[String]]] =
    LsFiles(basePath, succeed(_: Either[Throwable, Seq[String]]))

  def copyResource(
      resource: String,
      destinationPath: Path
  ): CopyResource[Either[Throwable, Unit]] =
    CopyResource(resource, destinationPath, succeed(_: Either[Throwable, Unit]))

  def writeFile(
      path: Path,
      content: String
  ): WriteFile[Either[Throwable, Unit]] =
    WriteFile(path, content, succeed(_: Either[Throwable, Unit]))

}
