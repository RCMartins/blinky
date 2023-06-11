package blinky.run

import blinky.TestSpec._
import blinky.run.TestInstruction._
import blinky.run.config.OptionsConfig
import os.Path
import zio.test._
import zio.{ExitCode, Scope}

object RunMutationsTest extends ZIOSpecDefault {

  private val instance = RunMutations
  private val projectPath: Path = Path(getFilePath("some-project"))
  private val mutantsOutputFile: String = getFilePath("blinky-empty.mutants")
  private val someException = SomeException("some exception")

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunMutations")(
      suite("run")(
        test("return success with a messge if no mutants found") {
          testInstruction(
            instance.run(
              projectPath,
              mutantsOutputFile,
              OptionsConfig.default,
            ),
            TestReadFile(
              Path(mutantsOutputFile),
              Left(someException),
              TestPrintErrorLine(
                s"""Blinky failed to load mutants file:
                   |$mutantsOutputFile
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
