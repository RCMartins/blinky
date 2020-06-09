package blinky.run

import ammonite.ops.pwd
import blinky.TestSpec
import blinky.run.Instruction._
import blinky.run.TestInstruction._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object SetupTest extends TestSpec {

  private val path = pwd

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Setup")(
      suite("setupCoursier")(
        test("return the correct instruction when 'coursier' is available") {
          testInstruction(
            Setup.setupCoursier(path),
            TestRunAsyncSuccess(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = true,
              TestReturn("coursier")
            )
          )
        },
        test("return the correct instruction when 'cs' is available") {
          testInstruction(
            Setup.setupCoursier(path),
            TestRunAsyncSuccess(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = false,
              TestRunAsyncSuccess(
                "cs",
                Seq("--help"),
                Map.empty,
                path,
                mockResult = true,
                TestReturn("cs")
              )
            )
          )
        },
        test("return the correct instruction when 'coursier and 'cs' is unavailable") {
          testInstruction(
            Setup.setupCoursier(path),
            TestRunAsyncSuccess(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = false,
              TestRunAsyncSuccess(
                "cs",
                Seq("--help"),
                Map.empty,
                path,
                mockResult = false,
                TestCopyResource(
                  "/coursier",
                  path / "coursier",
                  TestRunSync("chmod", Seq("+x", "coursier"), path, TestReturn("./coursier"))
                )
              )
            )
          )
        }
      ),
      suite("sbtCompileWithSemanticDB")(
        test("return the correct instruction") {
          testInstruction(
            Setup.sbtCompileWithSemanticDB(path),
            TestRunSync(
              "sbt",
              Seq(
                "set ThisBuild / semanticdbEnabled := true",
                "set ThisBuild / semanticdbVersion := \"4.3.12\"",
                "compile"
              ),
              path,
              TestReturn(())
            )
          )
        }
      ),
      suite("setupScalafix")(
        test("return the correct instruction") {
          testInstruction(
            Setup.setupScalafix(path),
            TestCopyResource(
              "/scalafix",
              path / "scalafix",
              TestRunSync("chmod", Seq("+x", "scalafix"), path, TestReturn(()))
            )
          )
        }
      )
    )

  private def testInstruction[A](
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
        ??? //assert(value1)(equalTo(value2))
    }

  // TODO - how to mock / create a good interpreter to test instruction elems ???

}
