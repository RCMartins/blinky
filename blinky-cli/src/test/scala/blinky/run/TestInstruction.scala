package blinky.run

import blinky.run.Instruction._
import os.{Path, RelPath}
import zio.test.{TestResult, assertTrue}

trait TestInstruction[+A]

object TestInstruction {

  final case class TestReturn[A](value: A) extends TestInstruction[A]

  final case class TestPrintLine[A](
      line: String,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestPrintErrorLine[A](
      line: String,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestRunStream[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      mockResult: Either[Throwable, Unit],
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestRunResultEither[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path,
      mockResult: Either[Throwable, String],
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestRunResultTimeout[A](
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      timeout: Long,
      path: Path,
      mockResult: Either[Throwable, TimeoutResult],
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestMakeTemporaryDirectory[A](
      mockResult: Path,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestMakeDirectory[A](
      path: Path,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestCopyInto[A](
      from: Path,
      to: Path,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestCopyResource[A](
      resource: String,
      destinationPath: Path,
      mockResult: Either[Throwable, Unit],
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestWriteFile[A](
      path: Path,
      content: String,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestReadFile[A](
      path: Path,
      mockResult: String,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestIsFile[A](
      path: Path,
      mockResult: Boolean,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestCopyRelativeFiles[A](
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path,
      mockResult: Either[Throwable, Unit],
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestLsFiles[A](
      basePath: Path,
      mockResult: Either[Throwable, Seq[String]],
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  def testInstruction[A](
      actualInstruction: Instruction[A],
      expectationInstruction: TestInstruction[A]
  ): TestResult =
    (actualInstruction, expectationInstruction) match {
      case (Return(value1), TestReturn(value2)) =>
        assertTrue(value1() == value2)
      case (PrintLine(line1, next1), TestPrintLine(line2, next2)) =>
        assertTrue(line1 == line2) &&
        testInstruction(next1, next2)
      case (PrintErrorLine(line1, next1), TestPrintErrorLine(line2, next2)) =>
        assertTrue(line1 == line2) &&
        testInstruction(next1, next2)
      case (
            RunStream(op1, args1, envArgs1, path1, next1),
            TestRunStream(op2, args2, envArgs2, path2, mockResult, next2)
          ) =>
        assertTrue(op1 == op2, args1 == args2, envArgs1 == envArgs2, path1 == path2) &&
        testInstruction(next1(mockResult), next2)
      case (
            RunResultEither(op1, args1, envArgs1, path1, next1),
            TestRunResultEither(op2, args2, envArgs2, path2, mockResult, next2)
          ) =>
        assertTrue(op1 == op2, args1 == args2, envArgs1 == envArgs2, path1 == path2) &&
        testInstruction(next1(mockResult), next2)
      case (
            RunResultTimeout(op1, args1, envArgs1, timeout1, path1, next1),
            TestRunResultTimeout(op2, args2, envArgs2, timeout2, path2, mockResult, next2)
          ) =>
        assertTrue(
          op1 == op2,
          args1 == args2,
          envArgs1 == envArgs2,
          timeout1 == timeout2,
          path1 == path2
        ) &&
        testInstruction(next1(mockResult), next2)
      case (
            CopyResource(resource1, destinationPath1, next1),
            TestCopyResource(resource2, destinationPath2, mockResult, next2)
          ) =>
        assertTrue(resource1 == resource2, destinationPath1 == destinationPath2) &&
        testInstruction(next1(mockResult), next2)
      case (
            CopyRelativeFiles(filesToCopy1, fromPath1, toPath1, next1),
            TestCopyRelativeFiles(filesToCopy2, fromPath2, toPath2, mockResult, next2)
          ) =>
        assertTrue(filesToCopy1 == filesToCopy2, fromPath1 == fromPath2, toPath1 == toPath2) &&
        testInstruction(next1(mockResult), next2)
      case (
            LsFiles(basePath1, next1),
            TestLsFiles(basePath2, mockResult, next2)
          ) =>
        assertTrue(basePath1 == basePath2) &&
        testInstruction(next1(mockResult), next2)
      case (
            IsFile(path1, next1),
            TestIsFile(path2, mockResult, next2),
          ) =>
        assertTrue(path1 == path2) &&
        testInstruction(next1(mockResult), next2)
      case (other1, other2) =>
        println(
          s"""elem1 Class: ${other1.getClass.getSimpleName}
             |elem2 Class: ${other2.getClass.getSimpleName}
             |elem1: $other1
             |elem2: $other2
             |""".stripMargin
        )
        ???
    }

}
