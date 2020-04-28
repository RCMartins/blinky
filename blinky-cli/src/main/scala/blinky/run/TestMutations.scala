package blinky.run

import ammonite.ops._
import play.api.libs.json.Json

import scala.util.{Failure, Random, Success, Try}

object TestMutations {
  def run(
      projectPath: Path,
      options: OptionsConfig
  ): Unit = {
    val mutationReport: List[Mutant] =
      read(projectPath / "mutations.json").split("\n").map(Json.parse(_).as[Mutant]).toList
    val testCommand = options.testCommand

    val numberOfMutants = mutationReport.length
    println(s"$numberOfMutants mutants found.")
    if (numberOfMutants == 0) {
      println("Try changing the mutation settings.")
    } else {
      %('sbt, 'bloopInstall)(projectPath)
      println("Running tests with original config")
      val compileResult =
        Try(%%('bash, "-c", s"bloop compile ${escapeString(options.compileCommand)}")(projectPath))
      compileResult match {
        case Failure(error) =>
          val newIssueLink = "https://github.com/RCMartins/blinky/issues/new"
          println(error)
          println(
            red(
              s"""There are compile errors after applying the Blinky rule.
                 |This is likely due to a bug in Blinky.
                 |If you want to report it use $newIssueLink""".stripMargin
            )
          )
          System.exit(1)
        case Success(_) =>
          val originalTestInitialTime = System.currentTimeMillis()
          val vanillaResult = Try(
            %%('bash, "-c", s"bloop test ${escapeString(testCommand)}")(projectPath)
          )
          vanillaResult match {
            case Failure(error) =>
              println("Tests failed... No mutations will run until this is fixed...")
              println(error)
              System.exit(1)
            case Success(result) =>
              println(green("Original tests passed..."))
              if (options.verbose)
                println(result.out.string)
              val originalTestTime = System.currentTimeMillis() - originalTestInitialTime
              if (options.verbose)
                println(green("time: " + originalTestTime))
              if (!options.dryRun) {
                val mutationsToTest =
                  if (originalTestTime * mutationReport.size >= options.maxRunningTime.toMillis)
                    Random.shuffle(mutationReport)
                  else
                    mutationReport

                println(
                  s"Running the same tests on mutated code (maximum of ${options.maxRunningTime})"
                )

                val initialTime = System.currentTimeMillis()

                val results = runMutations(mutationsToTest, initialTime)

                val mutantsToTestSize = results.size
                val mutantsToTestPerc = mutantsToTestSize * 100 / numberOfMutants
                val totalKilled = results.count(_._2)
                val totalNotKilled = mutantsToTestSize - results.count(_._2)
                val score = (totalKilled * 100.0) / mutantsToTestSize
                val scoreFormatted = "%4.1f".format(score)
                val totalTime = System.currentTimeMillis() - initialTime
                val avgTime = totalTime / 1000.0 / mutantsToTestSize
                val avgTimeFormatted = "%3.1f".format(avgTime)
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
                  if (score < options.mutationMinimum) {
                    println(
                      red(
                        s"Mutation score is below minimum " +
                          s"[$scoreFormatted% < ${options.mutationMinimum}%]"
                      )
                    )
                    System.exit(-1)
                  } else {
                    println(
                      green(
                        s"Mutation score is above minimum " +
                          s"[$scoreFormatted% \u2265 ${options.mutationMinimum}%]"
                      )
                    )
                  }
                }
              }
          }
      }
    }

    def runMutations(mutants: List[Mutant], initialTime: Long): List[(Int, Boolean)] = {
      mutants match {
        case Nil =>
          Nil
        case _ if System.currentTimeMillis() - initialTime > options.maxRunningTime.toMillis =>
          println(
            s"Timed out - maximum of ${options.maxRunningTime} " +
              s"(this can be changed in options.maxRunningTime)"
          )
          Nil
        case mutant :: othersMutants =>
          val id = mutant.id
          val time = System.currentTimeMillis()

//        val testResult = runInSbt(id)
          val testResult = runInBloop(id)

          val result =
            if (testResult.isSuccess) {
              println(s"Mutant #$id was not killed!")
              println(prettyDiff(mutant.diff, projectPath.toString))
              id -> false
            } else {
              println(s"Mutant #$id was killed.")
              id -> true
            }
          if (options.verbose) {
            println(s"time: ${System.currentTimeMillis() - time}")
            println("-" * 40)
          }

          result :: runMutations(othersMutants, initialTime)
      }
    }

    def runInSbt(mutantId: Int): Try[CommandResult] = {
      if (options.verbose)
        println(
          s"""> [SCALA_MUTATION_$mutantId=1] sbt "$testCommand""""
        )

      Try(
        Command(Vector.empty, Map(s"SCALA_MUTATION_$mutantId" -> "1"), Shellout.executeStream)(
          'sbt,
          testCommand
        )(projectPath)
      )
    }

    def runInBloop(mutantId: Int): Try[CommandResult] = {
      if (options.verbose)
        println(
          s"""> [SCALA_MUTATION_$mutantId=1] bash -c "bloop test ${escapeString(testCommand)}""""
//          s"""> bloop test "$testCommand" -- -DSCALA_MUTATION_$mutantId=1"""
        )

      Try(
        Command(Vector.empty, Map(s"SCALA_MUTATION_$mutantId" -> "1"), Shellout.executeStream)(
          'bash,
          "-c",
          s"bloop test ${escapeString(testCommand)}"
        )(projectPath)
//        %%(
//          "bloop",
//          "test",
//          testCommand,
//          "--",
//          s"-DSCALA_MUTATION_$mutantId=1"
//        )(projectPath)
      )
    }
  }

  private def red(str: String): String = s"\u001B[31m" + str + "\u001B[0m"

  private def green(str: String): String = s"\u001B[32m" + str + "\u001B[0m"

  private def prettyDiff(diffLines: List[String], projectPath: String): String = {
    val MinusRegex = "(^\\s*\\d+: -.*)".r
    val PlusRegex = "(^\\s*\\d+: +.*)".r
    diffLines
      .map {
        case MinusRegex(line) => red(line)
        case PlusRegex(line)  => green(line)
        case line             => sprintPathPrefix(line, projectPath)
      }
      .mkString("\n")
  }

  private def escapeString(str: String): String = {
    str.replace("\"", "\\\"")
  }

  private def sprintPathPrefix(string: String, pathPrefix: String): String = {
    val pos = string.indexOf(pathPrefix)
    if (pos == -1) string else string.substring(pos + pathPrefix.length)
  }
}
