package blinky.run

import os.{Path, pwd}
import blinky.{BuildInfo, TestSpec}
import blinky.run.TestInstruction._
import zio.test._
import zio.test.environment.TestEnvironment

object SetupTest extends TestSpec {

  private val path: Path = pwd

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Setup")(
      suite("setupCoursier")(
        test("return the correct instruction when 'coursier' is available") {
          testInstruction(
            Setup.setupCoursier(path),
            TestRunResultEither(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = Right(""),
              TestReturn("coursier")
            )
          )
        },
        test("return the correct instruction when 'cs' is available") {
          testInstruction(
            Setup.setupCoursier(path),
            TestRunResultEither(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = Left(new Throwable()),
              TestRunResultEither(
                "cs",
                Seq("--help"),
                Map.empty,
                path,
                mockResult = Right(""),
                TestReturn("cs")
              )
            )
          )
        },
        test("return the correct instruction when 'coursier and 'cs' is unavailable") {
          testInstruction(
            Setup.setupCoursier(path),
            TestRunResultEither(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = Left(new Throwable()),
              TestRunResultEither(
                "cs",
                Seq("--help"),
                Map.empty,
                path,
                mockResult = Left(new Throwable()),
                TestCopyResource(
                  "/coursier",
                  path / "coursier",
                  Right(()),
                  TestRunStream(
                    "chmod",
                    Seq("+x", "coursier"),
                    Map.empty,
                    path,
                    Right(()),
                    TestReturn("./coursier")
                  )
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
            TestRunStream(
              "sbt",
              Seq(
                "set Global / semanticdbEnabled := true",
                s"""set Global / semanticdbVersion := "${BuildInfo.semanticdbVersion}"""",
                "compile"
              ),
              Map("BLINKY" -> "true"),
              path,
              Right(()),
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
              Right(()),
              TestRunStream(
                "chmod",
                Seq("+x", "scalafix"),
                Map.empty,
                path,
                Right(()),
                TestReturn(())
              )
            )
          )
        }
      )
    )

}
