package blinky.run

import os.{Path, RelPath}
import blinky.run.Instruction._
import zio.test.Assertion.equalTo
import zio.test.{TestResult, assert}

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

  final case class TestLsFiles[+A](
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
        assert(value1())(equalTo(value2))
      case (PrintLine(line1, next1), TestPrintLine(line2, next2)) =>
        assert(line1)(equalTo(line2)) &&
        testInstruction(next1, next2)
      case (
            RunStream(op1, args1, envArgs1, path1, next1),
            TestRunStream(op2, args2, envArgs2, path2, mockResult, next2)
          ) =>
        assert(op1)(equalTo(op2)) &&
        assert(args1)(equalTo(args2)) &&
        assert(envArgs1)(equalTo(envArgs2)) &&
        assert(path1)(equalTo(path2)) &&
        testInstruction(next1(mockResult), next2)
      case (
            RunResultEither(op1, args1, envArgs1, path1, next1),
            TestRunResultEither(op2, args2, envArgs2, path2, mockResult, next2)
          ) =>
        assert(op1)(equalTo(op2)) &&
        assert(args1)(equalTo(args2)) &&
        assert(envArgs1)(equalTo(envArgs2)) &&
        assert(path1)(equalTo(path2)) &&
        testInstruction(next1(mockResult), next2)
      case (
            RunResultTimeout(op1, args1, envArgs1, timeout1, path1, next1),
            TestRunResultTimeout(op2, args2, envArgs2, timeout2, path2, mockResult, next2)
          ) =>
        assert(op1)(equalTo(op2)) &&
        assert(args1)(equalTo(args2)) &&
        assert(envArgs1)(equalTo(envArgs2)) &&
        assert(timeout1)(equalTo(timeout2)) &&
        assert(path1)(equalTo(path2)) &&
        testInstruction(next1(mockResult), next2)
      case (
            CopyResource(resource1, destinationPath1, next1),
            TestCopyResource(resource2, destinationPath2, mockResult, next2)
          ) =>
        assert(resource1)(equalTo(resource2)) &&
        assert(destinationPath1)(equalTo(destinationPath2)) &&
        testInstruction(next1(mockResult), next2)
      case (
            CopyRelativeFiles(filesToCopy1, fromPath1, toPath1, next1),
            TestCopyRelativeFiles(filesToCopy2, fromPath2, toPath2, mockResult, next2)
          ) =>
        assert(filesToCopy1)(equalTo(filesToCopy2)) &&
        assert(fromPath1)(equalTo(fromPath2)) &&
        assert(toPath1)(equalTo(toPath2)) &&
        testInstruction(next1(mockResult), next2)
      case (
            LsFiles(basePath1, next1),
            TestLsFiles(basePath2, mockResult, next2)
          ) =>
        assert(basePath1)(equalTo(basePath2)) &&
        testInstruction(next1(mockResult), next2)
      case (other1, other2) =>
        println(
          s"""elem1: ${println(other1.getClass.getSimpleName)}
             |elem2: ${println(other2.getClass.getSimpleName)}
             |""".stripMargin
        )
        ???
    }

}
