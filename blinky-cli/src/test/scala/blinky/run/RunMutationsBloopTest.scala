package blinky.run

import blinky.TestSpec._
import blinky.run.TestInstruction._
import os.Path
import zio.Scope
import zio.test._

object RunMutationsBloopTest extends ZIOSpecDefault {

  private val projectPath: Path = Path(getFilePath("some-project"))

  private val instance = new RunMutationsBloop(projectPath)
  private val someException = SomeException("some exception")

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunMutationsBloop")(
      suite("initializeRunner")(
        test("run the correct initializeRunner command on right") {
          testInstruction(
            instance.initializeRunner,
            TestRunStream(
              "sbt",
              Seq("bloopInstall"),
              Map("BLINKY" -> "true"),
              projectPath,
              Right(()),
              TestReturn(Right(()))
            )
          )
        },
        test("run the correct initializeRunner command on error") {
          testInstruction(
            instance.initializeRunner,
            TestRunStream(
              "sbt",
              Seq("bloopInstall"),
              Map("BLINKY" -> "true"),
              projectPath,
              Left(someException),
              TestReturn(Left(someException))
            )
          )
        },
      ),
      suite("initialCompile")(
        test("run the correct initialCompile command on right") {
          testInstruction(
            instance.initialCompile("\"core\""),
            TestRunResultEither(
              "bloop",
              Seq("compile", "\\\"core\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Right(""),
              TestReturn(Right(()))
            )
          )
        },
        test("run the correct initialCompile command on error") {
          testInstruction(
            instance.initialCompile("\"core\""),
            TestRunResultEither(
              "bloop",
              Seq("compile", "\\\"core\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Left(someException),
              TestReturn(Left(someException))
            )
          )
        },
      ),
      suite("vanillaTestRun")(
        test("run the correct test command on right") {
          testInstruction(
            instance.vanillaTestRun("core -o CoreTest -- -z \"test name\""),
            TestRunResultEither(
              "bash",
              Seq("-c", "bloop test core -o CoreTest -- -z \\\"test name\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Right(""),
              TestReturn(Right(""))
            )
          )
        },
        test("run the correct test command on error") {
          testInstruction(
            instance.vanillaTestRun("core -o CoreTest -- -z \"test name\""),
            TestRunResultEither(
              "bash",
              Seq("-c", "bloop test core -o CoreTest -- -z \\\"test name\\\""),
              Map("BLINKY" -> "true"),
              projectPath,
              Left(someException),
              TestReturn(Left(someException))
            )
          )
        },
      ),
      suite("cleanRunnerAfter")(
        test("do nothing when there is no timeout") {
          testInstruction(
            instance.cleanRunnerAfter(projectPath, List((1, RunResult.MutantSurvived))),
            TestReturn(())
          )
        },
        test("run the correct exit command when there is a timeout") {
          testInstruction(
            instance.cleanRunnerAfter(projectPath, List((1, RunResult.Timeout))),
            TestRunResultEither(
              "bash",
              Seq("-c", "bloop exit"),
              Map.empty,
              projectPath,
              Right(""),
              TestReturn(())
            )
          )
        },
        test("write a print when exit command fails") {
          testInstruction(
            instance.cleanRunnerAfter(projectPath, List((1, RunResult.Timeout))),
            TestRunResultEither(
              "bash",
              Seq("-c", "bloop exit"),
              Map.empty,
              projectPath,
              Left(someException),
              TestPrintErrorLine(
                s"""Failed to run 'bloop exit' after a mutation timeout.
                   |blinky.TestSpec$$SomeException: some exception""".stripMargin,
                TestReturn(()),
              )
            )
          )
        },
      ),
    )

}
