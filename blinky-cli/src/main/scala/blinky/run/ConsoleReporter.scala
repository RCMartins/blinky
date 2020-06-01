package blinky.run

import blinky.run.Utils._

object ConsoleReporter {

  def reportMutationResult(
      results: Seq[(Int, Boolean)],
      totalTime: Long,
      numberOfMutants: Int,
      options: OptionsConfig
  ): Boolean = {
    val mutantsToTestSize = results.size
    val mutantsToTestPerc = mutantsToTestSize * 100 / numberOfMutants
    val totalKilled = results.count(_._2)
    val totalNotKilled = mutantsToTestSize - results.count(_._2)
    val score = (totalKilled * 1000.0 / mutantsToTestSize).floor / 10.0
    val scoreFormatted = "%4.1f".format(score)
    val avgTimeFormatted = {
      val avgTime = (totalTime / 100.0 / mutantsToTestSize).ceil / 10.0
      "%3.1f".format(avgTime)
    }
    println(
      s"""
         |Mutation Results:
         |Total mutants found: $numberOfMutants
         |Total mutants tested: $mutantsToTestSize  ($mutantsToTestPerc%)
         |
         |Total Time (seconds): ${totalTime / 1000}
         |Average time each (seconds): $avgTimeFormatted
         |
         |Mutants Killed: ${green(totalKilled.toString)}
         |Mutants Not Killed: ${red(totalNotKilled.toString)}
         |Score: $scoreFormatted%
         |""".stripMargin
    )

    if (options.failOnMinimum) {
      val minimum = (options.mutationMinimum * 10.0).floor / 10.0
      if (score < minimum) {
        println(
          red(s"Mutation score is below minimum [$scoreFormatted% < $minimum%]")
        )
        false
      } else {
        println(
          green(s"Mutation score is above minimum [$scoreFormatted% >= $minimum%]")
        )
        true
      }
    } else
      true
  }

  def filesToMutateIsEmpty(): Unit =
    println(
      s"""${green("0 files to mutate because no code change found due to --mutateOnlyDiff flag.")}
         |If you want all files to be tested regardless use --mutateOnlyDiff=false
         |""".stripMargin
    )

}
