package blinky.run

import ammonite.ops.pwd
import blinky.TestSpec
import blinky.run.TestInstruction._
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

}
