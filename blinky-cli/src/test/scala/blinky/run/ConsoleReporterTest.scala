package blinky.run

import blinky.TestSpec
import blinky.run.ConsoleReporter._
import blinky.run.Instruction.PrintLine
import blinky.run.RunResult._
import blinky.run.config.OptionsConfig
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

import scala.annotation.tailrec

object ConsoleReporterTest extends TestSpec {

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("ConsoleReporter")(
      test("print the mutation score") {
        val (result, out) =
          testReportMutationResult(
            Seq((1, MutantKilled), (2, MutantKilled), (3, MutantSurvived)),
            1200L,
            numberOfMutants = 10,
            OptionsConfig.default.copy(failOnMinimum = false)
          )

        assert(out)(equalTo {
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
             |""".stripMargin
        }) &&
        assert(result)(equalTo(true))
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

        assert(out)(equalTo {
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
             |""".stripMargin
        }) &&
        assert(result)(equalTo(true))
      },
      test("print the mutation score when failOnMinimum flag is on (score < minimum)") {
        val (result, out) =
          testReportMutationResult(
            Seq((123, MutantKilled), (345, MutantSurvived), (456, MutantSurvived)),
            12301L,
            numberOfMutants = 34,
            OptionsConfig.default.copy(failOnMinimum = true, mutationMinimum = 33.4)
          )

        assert(out)(equalTo {
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
             |""".stripMargin
        }) &&
        assert(result)(equalTo(false))
      },
      suite("filesToMutateIsEmpty")(
        test("print the correct message used when the filesToMutate is empty") {
          val Instruction.PrintLine(line, _) = ConsoleReporter.filesToMutateIsEmpty
          assert(line)(equalTo {
            s"""${greenText(
              "0 files to mutate because no code change found due to --onlyMutateDiff flag."
            )}
               |If you want all files to be tested regardless use --onlyMutateDiff=false
               |""".stripMargin
          })
        }
      ),
      suite("gitIssues")(
        test("print the correct message used when the filesToMutate is empty") {
          val gitError =
            "fatal: ambiguous argument 'master': unknown revision or path not in the working tree."
          val Instruction.PrintLine(line, _) = ConsoleReporter.gitIssues(gitError)
          assert(line)(equalTo {
            s"""${redText("GIT command error:")}
               |$gitError
               |""".stripMargin
          })
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

  private def redText(str: String): String = s"\u001B[31m" + str + "\u001B[0m"

  private def greenText(str: String): String = s"\u001B[32m" + str + "\u001B[0m"

}
