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
  sbtCommand: String = "test"
): Unit = {
  run(
    projectPath,
    sbtCommand,
    OptionsConfig()
  )
}

def run(
  projectPath: Path,
  sbtCommand: String,
  options: OptionsConfig
): Unit = {
  val mutationReport: Seq[Mutation] = read(projectPath / "mutations.json").split("\n").toSeq.map(Json.parse(_).as[Mutation])

  val numberOfMutations = mutationReport.length
  println(s"$numberOfMutations mutations found.")
  if (numberOfMutations == 0) {
    println("Try changing the mutation settings.")
  } else {
    println("Running tests with original config")
    Try(%%('sbt, options.compileSbt)(projectPath))
    val originalTestInitialTime = System.currentTimeMillis()
    val vanillaResult = Try(%%('sbt, sbtCommand)(projectPath))
    vanillaResult match {
      case Failure(error) =>
        println("Tests failed... No mutations will run until this is fixed...")
        println(error)
      case Success(_) =>
        println(green("Original tests passed..."))
        if (!options.dryRun) {
          val originalTestTime = System.currentTimeMillis() - originalTestInitialTime
          val mutationsToTest =
            Random.shuffle(mutationReport)
              .take(Math.floor(options.maxRunningTime.toMillis / originalTestTime).toInt)
              .sortBy(_.id)
          println(s"Running the same tests on ${mutationsToTest.size} mutations...")

          val initialTime = System.currentTimeMillis()

          val results =
            for (mutation <- mutationsToTest) yield {
              val id = mutation.id
              val time = System.currentTimeMillis()

              if (options.verbose)
                println(s"""sbt ";set tests / javaOptions in Test += \"-DSCALA_MUTATION_$id\";$sbtCommand"""")

              val testResult =
                Try(%%(
                  'sbt,
                  s""";set tests / javaOptions in Test += \"-DSCALA_MUTATION_$id\";$sbtCommand"""
                )(projectPath))

              val result =
                if (testResult.isSuccess) {
                  println(s"Mutant #$id was not killed!")
                  println(prettyDiff(mutation.diff, projectPath.toString))
                  id -> false
                } else {
                  println(s"Mutant #$id was killed.")
                  id -> true
                }
              if (options.verbose)
                println(s"time: ${System.currentTimeMillis() - time}")

              result
            }

          val mutationsToTestSize = mutationsToTest.size
          val totalKilled = results.count(_._2)
          val totalNotKilled = mutationsToTestSize - results.count(_._2)
          val score = (totalKilled * 100) / mutationsToTestSize
          val totalTime = System.currentTimeMillis() - initialTime
          println(
            s"""
               |Mutation Results:
               |Total mutations found: $numberOfMutations
               |Total mutations tested: $mutationsToTestSize  (${mutationsToTestSize * 100 / numberOfMutations}%)
               |
               |Total Time (seconds): ${totalTime / 1000}
               |Average time each (seconds): ${totalTime / 1000 / mutationsToTestSize}
               |
               |Mutants Killed: ${green(totalKilled.toString)}
               |Mutants Not Killed: ${red(totalNotKilled.toString)}
               |Score: $score%
               |""".stripMargin)
        }
    }
  }
}

def red(str: String): String = s"\u001B[31m" + str + "\u001B[0m"

def green(str: String): String = s"\u001B[32m" + str + "\u001B[0m"

def prettyDiff(diffLines: List[String], projectPath: String): String = {
  val MinusRegex = "(^\\s*\\d+: -.*)".r
  val PlusRegex = "(^\\s*\\d+: +.*)".r
  diffLines.map {
    case MinusRegex(line) => red(line)
    case PlusRegex(line) => green(line)
    case line => line.stripPrefix(projectPath)

  }.mkString("\n")
}
