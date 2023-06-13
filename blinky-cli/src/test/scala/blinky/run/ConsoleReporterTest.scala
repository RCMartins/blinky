package blinky.run

import blinky.TestSpec._
import blinky.run.ConsoleReporter._
import blinky.run.Instruction.PrintLine
import blinky.run.RunResult._
import blinky.run.TestInstruction._
import blinky.run.config.OptionsConfig
import zio.ExitCode
import zio.test._

import scala.annotation.tailrec

object ConsoleReporterTest extends ZIOSpecDefault {

  val spec: Spec[TestEnvironment, TestFailure[Nothing]] =
    suite("ConsoleReporter")(
      test("print the mutation score") {
        val (result, out) =
          testReportMutationResult(
            Seq((1, MutantKilled), (2, MutantKilled), (3, MutantSurvived)),
            1200L,
            numberOfMutants = 10,
            OptionsConfig.default.copy(failOnMinimum = false)
          )

        assertTrue(
          out ==
            s"""
               |Mutation Results:
               |Total mutants found: 10
               |Total mutants tested: 3  (30%)
               |
               |Total Time (seconds): 1
               |Average time each (seconds): 0.4
               |
               |Mutants Killed: ${greenText("2")}
               |Mutants Not Killed: ${redText("1")}
               |Score: 66.6%
               |
               |""".stripMargin,
          result
        )
      },
      test("print the mutation score when failOnMinimum flag is on (score == minimum)") {
        val (result, out) =
          testReportMutationResult(
            Seq(
              (123, MutantKilled),
              (234, MutantKilled),
              (345, MutantSurvived),
              (456, MutantSurvived)
            ),
            12400L,
            numberOfMutants = 16,
            OptionsConfig.default.copy(failOnMinimum = true, mutationMinimum = 50.0)
          )

        assertTrue(
          out ==
            s"""
               |Mutation Results:
               |Total mutants found: 16
               |Total mutants tested: 4  (25%)
               |
               |Total Time (seconds): 12
               |Average time each (seconds): 3.1
               |
               |Mutants Killed: ${greenText("2")}
               |Mutants Not Killed: ${redText("2")}
               |Score: 50.0%
               |
               |${greenText("Mutation score is above minimum [50.0% >= 50.0%]")}
               |""".stripMargin,
          result
        )
      },
      test("print the mutation score when failOnMinimum flag is on (score < minimum)") {
        val (result, out) =
          testReportMutationResult(
            Seq((123, MutantKilled), (345, MutantSurvived), (456, MutantSurvived)),
            12301L,
            numberOfMutants = 34,
            OptionsConfig.default.copy(failOnMinimum = true, mutationMinimum = 33.4)
          )

        assertTrue(
          out ==
            s"""
               |Mutation Results:
               |Total mutants found: 34
               |Total mutants tested: 3  (8%)
               |
               |Total Time (seconds): 12
               |Average time each (seconds): 4.2
               |
               |Mutants Killed: ${greenText("1")}
               |Mutants Not Killed: ${redText("2")}
               |Score: 33.3%
               |
               |${redText("Mutation score is below minimum [33.3% < 33.4%]")}
               |""".stripMargin,
          !result
        )
      },
      suite("filesToMutateIsEmpty")(
        test("print the correct message used when the filesToMutate is empty") {
          testInstruction(
            ConsoleReporter.filesToMutateIsEmpty,
            TestPrintLine(
              s"""${greenText(
                  "0 files to mutate because no code change found due to --onlyMutateDiff flag."
                )}
                 |If you want all files to be tested regardless use --onlyMutateDiff=false
                 |""".stripMargin,
              TestReturn(())
            )
          )
        }
      ),
      suite("gitIssues")(
        test("print the correct message used when the filesToMutate is empty") {
          val gitError: String =
            "fatal: ambiguous argument 'master': unknown revision or path not in the working tree."
          testInstruction(
            ConsoleReporter.gitFailure(new Throwable(gitError)),
            TestPrintLine(
              s"""${redText("GIT command error:")}
                 |$gitError
                 |""".stripMargin,
              TestReturn(ExitCode.failure)
            )
          )
        }
      )
    )

  private def testReportMutationResult(
      results: Seq[(Int, RunResult)],
      totalTime: Long,
      numberOfMutants: Int,
      options: OptionsConfig
  ): (Boolean, String) = {

    @tailrec
    def getReturn(instruction: Instruction[Boolean], out: String): (Boolean, String) =
      instruction match {
        case Instruction.Return(value) =>
          (value(), out)
        case PrintLine(line, next) =>
          getReturn(next, out ++ line + "\n")
        case _ =>
          ???
      }

    getReturn(reportMutationResult(results, totalTime, numberOfMutants, options), "")
  }

}
