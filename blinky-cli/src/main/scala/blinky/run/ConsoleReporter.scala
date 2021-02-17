package blinky.run

import blinky.run.Instruction._
import blinky.run.Utils._
import blinky.run.config.OptionsConfig

object ConsoleReporter {

  def reportMutationResult(
      results: Seq[(Int, Boolean)],
      totalTime: Long,
      numberOfMutants: Int,
      options: OptionsConfig
  ): Instruction[Boolean] = {
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

    for {
      _ <- printLine(
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

      result <-
        if (options.failOnMinimum) {
          val minimum = (options.mutationMinimum * 10.0).floor / 10.0
          if (score < minimum)
            printLine(
              red(s"Mutation score is below minimum [$scoreFormatted% < $minimum%]")
            ).flatMap(_ => Instruction.succeed(false))
          else
            printLine(
              green(s"Mutation score is above minimum [$scoreFormatted% >= $minimum%]")
            ).flatMap(_ => Instruction.succeed(true))
        } else
          succeed(true)
    } yield result
  }

  val filesToMutateIsEmpty: Instruction[Unit] =
    printLine(
      s"""${green("0 files to mutate because no code change found due to --onlyMutateDiff flag.")}
         |If you want all files to be tested regardless use --onlyMutateDiff=false
         |""".stripMargin
    )
  def gitIssues(error: String): Instruction[Unit] =
    printLine(red(s"git command error: $error"))

}
