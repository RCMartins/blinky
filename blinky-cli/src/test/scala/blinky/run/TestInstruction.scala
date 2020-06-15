package blinky.run

import ammonite.ops.Path
import blinky.run.Instruction._
import zio.test.Assertion.equalTo
import zio.test.{TestResult, assert}

sealed trait TestInstruction[+A]

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
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestWriteFile[A](
      path: Path,
      content: String,
      next: TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestReadFile[A](
      path: Path,
      next: String => TestInstruction[A]
  ) extends TestInstruction[A]

  final case class TestIsFile[A](
      path: Path,
      next: Boolean => TestInstruction[A]
  ) extends TestInstruction[A]

  def testSucceed[A](value: A): TestReturn[A] =
    TestReturn(value)

  def testConditional(cond: Boolean)(value: TestInstruction[Unit]): TestInstruction[Unit] =
    if (cond) value else testSucceed(())

  def testPrintLine(line: String): TestPrintLine[Unit] =
    TestPrintLine(line, testSucceed(()))

  def testPrintErrorLine(line: String): TestPrintErrorLine[Unit] =
    TestPrintErrorLine(line, testSucceed(()))

  def testRunSync(op: String, args: Seq[String])(path: Path): TestRunSync[Unit] =
    TestRunSync(op, args, path, testSucceed(()))

  def testRunAsync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty,
      path: Path,
      mockResult: String
  ): TestRunAsync[String] =
    TestRunAsync(op, args, envArgs, path, mockResult, testSucceed(mockResult))

  def testRunAsyncSuccess(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty,
      path: Path,
      mockResult: Boolean
  ): TestRunAsyncSuccess[Boolean] =
    TestRunAsyncSuccess(op, args, envArgs, path, mockResult, testSucceed(mockResult))

  def testRunAsyncEither(op: String, args: Seq[String], envArgs: Map[String, String] = Map.empty)(
      path: Path
  ): TestRunAsyncEither[Either[String, String]] =
    TestRunAsyncEither(op, args, envArgs, path, testSucceed(_: Either[String, String]))

  def testMakeTemporaryDirectory(mockResult: Path): TestMakeTemporaryDirectory[Path] =
    TestMakeTemporaryDirectory(mockResult, testSucceed(mockResult))

  def testMakeDirectory(path: Path): TestMakeDirectory[Unit] =
    TestMakeDirectory(path, testSucceed(()))

  def testCopyInto(from: Path, to: Path): TestCopyInto[Unit] =
    TestCopyInto(from, to, testSucceed(()))

  def testReadFile(path: Path): TestReadFile[String] =
    TestReadFile(path, content => testSucceed(content))

  implicit class ConsoleSyntax[+A](self: TestInstruction[A]) {
    def map[B](f: A => B): TestInstruction[B] =
      flatMap(a => TestReturn(f(a)))

    def flatMap[B](
        f: A => TestInstruction[B]
    ): TestInstruction[B] =
      self match {
        case TestReturn(value) =>
          f(value)
        case TestPrintLine(line, next) =>
          TestPrintLine(line, next.flatMap(f))
        case TestPrintErrorLine(line, next) =>
          TestPrintErrorLine(line, next.flatMap(f))
        case TestRunSync(op, args, path, next) =>
          TestRunSync(op, args, path, next.flatMap(f))
        case TestRunAsync(op, args, envArgs, path, mockResult, next) =>
          TestRunAsync(op, args, envArgs, path, mockResult, next.flatMap(f))
        case TestRunAsyncSuccess(op, args, envArgs, path, mockResult, next) =>
          TestRunAsyncSuccess(op, args, envArgs, path, mockResult, next.flatMap(f))
        case TestRunAsyncEither(op, args, envArgs, path, next) =>
          TestRunAsyncEither(op, args, envArgs, path, next(_: Either[String, String]).flatMap(f))
        case TestMakeTemporaryDirectory(mockResult, next) =>
          TestMakeTemporaryDirectory(mockResult, next.flatMap(f))
        case TestMakeDirectory(path, next) =>
          TestMakeDirectory(path, next.flatMap(f))
        case TestCopyInto(from, to, next) =>
          TestCopyInto(from, to, next.flatMap(f))
        case TestCopyResource(resource, destinationPath, next) =>
          TestCopyResource(resource, destinationPath, next.flatMap(f))
        case TestWriteFile(path, content, next) =>
          TestWriteFile(path, content, next.flatMap(f))
        case TestReadFile(path, next) =>
          TestReadFile(path, next(_: String).flatMap(f))
        case TestIsFile(path, next) =>
          TestIsFile(path, next(_: Boolean).flatMap(f))
      }
  }

  def testInstruction[A](
      actualInstruction: Instruction[A],
      expectationInstruction: TestInstruction[A]
  ): TestResult =
    (actualInstruction, expectationInstruction) match {
      case (Return(value1), TestReturn(value2)) =>
        assert(value1())(equalTo(value2))
      case (
            RunSync(op1, args1, path1, next1),
            TestRunSync(op2, args2, path2, next2)
          ) =>
        assert(op1)(equalTo(op2)) &&
          assert(args1)(equalTo(args2)) &&
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
        println(other1.getClass.getSimpleName)
        println(other2.getClass.getSimpleName)
        ???
    }

}
