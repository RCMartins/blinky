package blinky.run

import blinky.TestSpec._
import blinky.run.TestInstruction._
import os.Path
import zio.Scope
import zio.test._

object RunMutationsSBTTest extends ZIOSpecDefault {

  private val projectPath: Path = Path(getFilePath("some-project"))

  private val instance = RunMutationsSBT
  private val someException = SomeException("some exception")

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunMutationsSBT")(
      suite("initializeRunner")(
        test("should be empty") {
          testInstruction(
            instance.initializeRunner(projectPath),
            TestReturn(Right(()))
          )
        },
      ),
      suite("initialCompile")(
        test("run the correct sbt command on right") {
          testInstruction(
            instance.initialCompile(projectPath, "\"compile\""),
            TestRunResultEither(
              "sbt",
              Seq("\\\"compile\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Right(""),
              TestReturn(Right(()))
            )
          )
        },
        test("run the correct sbt command on error") {
          testInstruction(
            instance.initialCompile(projectPath, "\"compile\""),
            TestRunResultEither(
              "sbt",
              Seq("\\\"compile\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Left(someException),
              TestReturn(Left(someException))
            )
          )
        },
      ),
      suite("vanillaTestRun")(
        test("run the correct sbt test command on right") {
          testInstruction(
            instance.vanillaTestRun(projectPath, "testOnly -- -z \"test name\""),
            TestRunResultEither(
              "bash",
              Seq("-c", "sbt  testOnly -- -z \\\"test name\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Right(""),
              TestReturn(Right(""))
            )
          )
        },
        test("run the correct sbt test command on error") {
          testInstruction(
            instance.vanillaTestRun(projectPath, "testOnly -- -z \"test name\""),
            TestRunResultEither(
              "bash",
              Seq("-c", "sbt  testOnly -- -z \\\"test name\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Left(someException),
              TestReturn(Left(someException))
            )
          )
        },
      ),
      suite("cleanRunnerAfter")(
        test("should be empty") {
          testInstruction(
            instance.cleanRunnerAfter(projectPath, Nil),
            TestReturn(())
          )
        },
      ),
    )

}
