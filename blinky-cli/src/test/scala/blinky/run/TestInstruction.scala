package blinky.run

import ammonite.ops.{Path, RelPath}
import blinky.run.Instruction.{CopyResource, Return, RunAsyncSuccess, RunSync}
import zio.test.Assertion.equalTo
import zio.test.{TestResult, assert}

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
      envArgs: Map[String, String],
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

  final case class TestCopyRelativeFiles[A](
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path,
      mockResult: Either[Throwable, Unit],
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  def testInstruction[A](
      actualInstruction: Instruction[A],
      expectationInstruction: TestInstruction[A]
  ): TestResult =
    (actualInstruction, expectationInstruction) match {
      case (Return(value1), TestReturn(value2)) =>
        assert(value1())(equalTo(value2))
      case (
            RunSync(op1, args1, envArgs1, path1, next1),
            TestRunSync(op2, args2, envArgs2, path2, next2)
          ) =>
        assert(op1)(equalTo(op2)) &&
          assert(args1)(equalTo(args2)) &&
          assert(envArgs1)(equalTo(envArgs2)) &&
          assert(path1)(equalTo(path2)) &&
          testInstruction(next1, next2)
      case (
            RunAsyncSuccess(op1, args1, envArgs1, path1, next1),
            TestRunAsyncSuccess(op2, args2, envArgs2, path2, mockResult, next2)
          ) =>
        assert(op1)(equalTo(op2)) &&
          assert(args1)(equalTo(args2)) &&
          assert(envArgs1)(equalTo(envArgs2)) &&
          assert(path1)(equalTo(path2)) &&
          testInstruction(next1(mockResult), next2)
      case (
            CopyResource(resource1, destinationPath1, next1),
            TestCopyResource(resource2, destinationPath2, next2)
          ) =>
        assert(resource1)(equalTo(resource2)) &&
          assert(destinationPath1)(equalTo(destinationPath2)) &&
          testInstruction(next1, next2)
      case (other1, other2) =>
        println(
          s"""elem1: ${println(other1.getClass.getSimpleName)}
             |elem2: ${println(other2.getClass.getSimpleName)}
             |""".stripMargin
        )
        ???
    }

}
