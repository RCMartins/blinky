package blinky.run

import blinky.TestSpec._
import blinky.run.TestInstruction._
import blinky.run.config.OptionsConfig
import os.Path
import zio.test._
import zio.{ExitCode, Scope}

object RunMutationsTest extends ZIOSpecDefault {

  private lazy val projectPath: Path = Path(getFilePath("some-project"))
  private lazy val instance = new RunMutations(new RunMutationsSBT(projectPath))
  private lazy val emptyMutantsOutputFile: String = getFilePath("blinky-empty.mutants")
  private lazy val someException = SomeException("some exception")

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunMutations")(
      suite("run")(
        test("return success with a messge if no mutants found") {
          testInstruction(
            instance.run(
              projectPath,
              emptyMutantsOutputFile,
              OptionsConfig.default,
            ),
            TestReadFile(
              Path(emptyMutantsOutputFile),
              Left(someException),
              TestPrintErrorLine(
                s"""Blinky failed to load mutants file:
                   |$emptyMutantsOutputFile
                   |""".stripMargin,
                TestPrintLine(
                  "0 mutants found in 0 scala files.",
                  TestPrintLine(
                    "Try changing the mutation settings.",
                    TestReturn(ExitCode.success),
                  ),
                ),
              ),
            )
          )
        },
      ),
    )

}
