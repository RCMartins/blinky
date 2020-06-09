package blinky.run

import ammonite.ops.Path

trait TestInstruction[A]

object TestInstruction {

  final case class TestReturn[A](value: A) extends TestInstruction[A]

  final case class TestPrintLine[A](line: String, next: TestInstruction[A])
      extends TestInstruction[A]

  final case class TestPrintErrorLine[A](line: String, next: TestInstruction[A])
      extends TestInstruction[A]

  final case class TestRunSync[A](
      op: String,
      args: Seq[String],
      path: Path,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestRunAsync[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      mockResult: String,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestRunAsyncSuccess[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      mockResult: Boolean,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestRunAsyncEither[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      next: Either[String, String] => TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestMakeTemporaryDirectory[A](next: Path => TestInstruction[A])
      extends TestInstruction[A]

  final case class TestMakeDirectory[A](path: Path, next: TestInstruction[A])
      extends TestInstruction[A]

  final case class TestCopyInto[A](from: Path, to: Path, next: TestInstruction[A])
      extends TestInstruction[A]

  final case class TestCopyResource[A](
      resource: String,
      destinationPath: Path,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestWriteFile[A](path: Path, content: String, next: TestInstruction[A])
      extends TestInstruction[A]

  final case class TestReadFile[A](path: Path, next: String => TestInstruction[A])
      extends TestInstruction[A]

  final case class TestIsFile[A](path: Path, next: Boolean => TestInstruction[A])
      extends TestInstruction[A]

}
