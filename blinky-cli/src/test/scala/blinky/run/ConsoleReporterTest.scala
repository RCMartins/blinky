package blinky.run

import blinky.TestSpec
import blinky.run.ConsoleReporter._
import blinky.run.Instruction.PrintLine
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
            Seq((1, true), (2, true), (3, false)),
            1200L,
            numberOfMutants = 10,
            OptionsConfig.default.copy(failOnMinimum = false)
          )

        assert(out)(equalTo {
          """
            |Mutation Results:
            |Total mutants found: 10
            |Total mutants tested: 3  (30%)
            |
            |Total Time (seconds): 1
            |Average time each (seconds): 0.4
            |
            |Mutants Killed: \u001B[32m2\u001B[0m
            |Mutants Not Killed: \u001B[31m1\u001B[0m
            |Score: 66.6%
            |
            |""".stripMargin
        }) &&
        assert(result)(equalTo(true))
      },
      test("print the mutation score when failOnMinimum flag is on (score == minimum)") {
        val (result, out) =
          testReportMutationResult(
            Seq((123, true), (234, true), (345, false), (456, false)),
            12400L,
            numberOfMutants = 16,
            OptionsConfig.default.copy(failOnMinimum = true, mutationMinimum = 50.0)
          )

        assert(out)(equalTo {
          """
            |Mutation Results:
            |Total mutants found: 16
            |Total mutants tested: 4  (25%)
            |
            |Total Time (seconds): 12
            |Average time each (seconds): 3.1
            |
            |Mutants Killed: \u001B[32m2\u001B[0m
            |Mutants Not Killed: \u001B[31m2\u001B[0m
            |Score: 50.0%
            |
            |\u001B[32mMutation score is above minimum [50.0% >= 50.0%]\u001B[0m
            |""".stripMargin
        }) &&
        assert(result)(equalTo(true))
      },
      test("print the mutation score when failOnMinimum flag is on (score < minimum)") {
        val (result, out) =
          testReportMutationResult(
            Seq((123, true), (345, false), (456, false)),
            12301L,
            numberOfMutants = 34,
            OptionsConfig.default.copy(failOnMinimum = true, mutationMinimum = 33.4)
          )

        assert(out)(equalTo {
          """
            |Mutation Results:
            |Total mutants found: 34
            |Total mutants tested: 3  (8%)
            |
            |Total Time (seconds): 12
            |Average time each (seconds): 4.2
            |
            |Mutants Killed: \u001B[32m1\u001B[0m
            |Mutants Not Killed: \u001B[31m2\u001B[0m
            |Score: 33.3%
            |
            |\u001B[31mMutation score is below minimum [33.3% < 33.4%]\u001B[0m
            |""".stripMargin
        }) &&
        assert(result)(equalTo(false))
      },
      suite("filesToMutateIsEmpty")(
        test("print the correct message used when the filesToMutate is empty") {
          val Instruction.PrintLine(line, _) = ConsoleReporter.filesToMutateIsEmpty
          assert(line)(equalTo {
            s"""\u001B[32m0 files to mutate because no code change found due to --onlyMutateDiff flag.\u001B[0m
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
            s"""\u001B[31mGIT command error:\u001B[0m
               |$gitError
               |""".stripMargin
          })
        }
      )
    )

  private def testReportMutationResult(
      results: Seq[(Int, Boolean)],
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
