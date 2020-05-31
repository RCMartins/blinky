package blinky.run

import java.io.ByteArrayOutputStream

import blinky.TestSpec

class ConsoleReporterTest extends TestSpec {

  "reportMutationResult" should {

    "print the mutation score" in {
      val (successful, out, err) =
        testReportMutationResult(
          Seq((1, true), (2, true), (3, false)),
          1234L,
          numberOfMutants = 10,
          OptionsConfig.default.copy(failOnMinimum = false)
        )

      assert(successful)
      out mustEqual
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
      err mustEqual ""
    }

    "print the mutation score when failOnMinimum flag is on (score == minimum)" in {
      val (successful, out, err) =
        testReportMutationResult(
          Seq((123, true), (234, true), (345, false), (456, false)),
          12400L,
          numberOfMutants = 16,
          OptionsConfig.default.copy(failOnMinimum = true, mutationMinimum = 50.0)
        )

      assert(successful)
      out mustEqual
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
      err mustEqual ""
    }

    "print the mutation score when failOnMinimum flag is on (score < minimum)" in {
      val (successful, out, err) =
        testReportMutationResult(
          Seq((123, true), (345, false), (456, false)),
          12301L,
          numberOfMutants = 34,
          OptionsConfig.default.copy(failOnMinimum = true, mutationMinimum = 33.4)
        )

      assert(!successful)
      out mustEqual
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
      err mustEqual ""
    }

  }

  private def testReportMutationResult(
      results: Seq[(Int, Boolean)],
      totalTime: Long,
      numberOfMutants: Int,
      options: OptionsConfig
  ): (Boolean, String, String) = {
    val outCapture = new ByteArrayOutputStream
    val errCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      Console.withErr(errCapture) {
        val result =
          ConsoleReporter.reportMutationResult(
            results,
            totalTime,
            numberOfMutants,
            options
          )
        (
          result,
          removeCarriageReturns(outCapture.toString),
          removeCarriageReturns(errCapture.toString)
        )
      }
    }
  }

}
