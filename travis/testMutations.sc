import $file.common
import $ivy.`com.typesafe.play::play-json:2.7.3`
import ammonite.ops._
import common._
import play.api.libs.json.Json

import scala.util.{Failure, Random, Success, Try}

val path = pwd

@main
def main(
    projectPath: Path,
    testCommand: String = "test"
): Unit = {
  run(
    projectPath,
    testCommand,
    OptionsConfig()
  )
}

def run(
    projectPath: Path,
    testCommand: String,
    options: OptionsConfig
): Unit = {
  val mutationReport: Seq[Mutant] =
    read(projectPath / "mutations.json").split("\n").toSeq.map(Json.parse(_).as[Mutant])

  val numberOfMutants = mutationReport.length
  println(s"$numberOfMutants mutants found.")
  if (numberOfMutants == 0) {
    println("Try changing the mutation settings.")
  } else {
    %('sbt, 'bloopInstall)(projectPath)
    println("Running tests with original config")
    Try(%%('bash, "-c", s"""bloop "${options.compileCommand}"""")(projectPath))
    val originalTestInitialTime = System.currentTimeMillis()
    val vanillaResult = Try(%%('bash, "-c", s"""bloop test "$testCommand"""")(projectPath))
    vanillaResult match {
      case Failure(error) =>
        println("Tests failed... No mutations will run until this is fixed...")
        println(error)
        System.exit(1)
      case Success(_) =>
        println(green("Original tests passed..."))
        if (!options.dryRun) {
          val originalTestTime = System.currentTimeMillis() - originalTestInitialTime
          val mutationsToTest =
            Random
              .shuffle(mutationReport)
              .take(Math.floor(options.maxRunningTime.toMillis / originalTestTime).toInt)
              .sortBy(_.id)
              .toList
          println(s"Running the same tests on ${mutationsToTest.size} mutations...")

          val initialTime = System.currentTimeMillis()

          val results = runMutations(mutationsToTest, initialTime)

          val mutantsToTestSize = results.size
          val totalKilled = results.count(_._2)
          val totalNotKilled = mutantsToTestSize - results.count(_._2)
          val score = (totalKilled * 100.0) / mutantsToTestSize
          val scoreFormatted = "%4.1f".format(score)
          val totalTime = System.currentTimeMillis() - initialTime
          println(
            s"""
               |Mutation Results:
               |Total mutants found: $numberOfMutants
               |Total mutants tested: $mutantsToTestSize  (${mutantsToTestSize * 100 / numberOfMutants}%)
               |
               |Total Time (seconds): ${totalTime / 1000}
               |Average time each (seconds): ${totalTime / 1000 / mutantsToTestSize}
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
                  s"Mutation score is below minimum [$scoreFormatted% < ${options.mutationMinimum}%]"
                )
              )
              System.exit(1)
            } else {
              println(
                green(
                  s"Mutation score is above minimum [$scoreFormatted% \u2265 ${options.mutationMinimum}%]"
                )
              )
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
          s"Timed out - maximum of ${options.maxRunningTime} (this can be changed in options.maxRunningTime)"
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
        if (options.verbose)
          println(s"time: ${System.currentTimeMillis() - time}")

        result :: runMutations(othersMutants, initialTime)
    }
  }

  def runInSbt(mutantId: Int): Try[CommandResult] = {
    if (options.verbose)
      println(
        s"""[SCALA_MUTATION_$mutantId=1] sbt "$testCommand""""
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
        s"""bash -c "bloop test \"$testCommand\"""""
      )

    Try(
      Command(Vector.empty, Map(s"SCALA_MUTATION_$mutantId" -> "1"), Shellout.executeStream)(
        'bash,
        "-c",
        s"bloop test $testCommand"
      )(projectPath)
    )
  }
}

def red(str: String): String = s"\u001B[31m" + str + "\u001B[0m"

def green(str: String): String = s"\u001B[32m" + str + "\u001B[0m"

def prettyDiff(diffLines: List[String], projectPath: String): String = {
  val MinusRegex = "(^\\s*\\d+: -.*)".r
  val PlusRegex = "(^\\s*\\d+: +.*)".r
  diffLines
    .map {
      case MinusRegex(line) => red(line)
      case PlusRegex(line)  => green(line)
      case line             => line.stripPrefix(projectPath)

    }
    .mkString("\n")
}
