package blinky.run

import blinky.run.TestInstruction._
import blinky.{BuildInfo, TestSpec}
import os.{Path, pwd}
import zio.test._

object SetupTest extends TestSpec {

  private val path: Path = pwd

  val spec: Spec[TestEnvironment, TestFailure[Nothing]] =
    suite("Setup")(
      suite("setupCoursier")(
        test("return the correct instruction when 'coursier' is available") {
          testInstruction(
            Setup.setupCoursier(path),
            TestRunSyncSuccess(
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
            TestRunSyncSuccess(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = false,
              TestRunSyncSuccess(
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
            TestRunSyncSuccess(
              "coursier",
              Seq("--help"),
              Map.empty,
              path,
              mockResult = false,
              TestRunSyncSuccess(
                "cs",
                Seq("--help"),
                Map.empty,
                path,
                mockResult = false,
                TestCopyResource(
                  "/coursier",
                  path / "coursier",
                  TestRunSync(
                    "chmod",
                    Seq("+x", "coursier"),
                    Map.empty,
                    path,
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
            TestRunSync(
              "sbt",
              Seq(
                "set Global / semanticdbEnabled := true",
                s"""set Global / semanticdbVersion := "${BuildInfo.semanticdbVersion}"""",
                "compile"
              ),
              Map("BLINKY" -> "true"),
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
              TestRunSync(
                "chmod",
                Seq("+x", "scalafix"),
                Map.empty,
                path,
                TestReturn(())
              )
            )
          )
        }
      )
    )

}
