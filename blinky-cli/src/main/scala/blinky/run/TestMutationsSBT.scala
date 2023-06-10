package blinky.run

import blinky.internal.MutantFile
import blinky.run.Instruction._
import blinky.run.Setup.defaultEnvArgs
import blinky.run.Utils._
import blinky.run.config.OptionsConfig
import os.Path

class TestMutationsSBT(projectPath: Path) extends TestMutationsRunner {

  private val extraSbtParams: String = "" // --client

  def initializeRunner(): Instruction[Unit] =
    succeed(())

  def initialCompile(compileCommand: String): RunResultEither[Either[Throwable, String]] =
    runResultEither(
      "sbt",
      Seq(extraSbtParams, escapeString(compileCommand)).filter(_.nonEmpty),
      envArgs = defaultEnvArgs,
      path = projectPath
    )

  def vanillaTestRun(testCommand: String): RunResultEither[Either[Throwable, String]] =
    runBashEither(
      s"sbt $extraSbtParams ${escapeString(testCommand)}",
      envArgs = defaultEnvArgs,
      path = projectPath
    )

  def runMutant(
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutant: MutantFile
  ): Instruction[RunResult] = {
    val prints =
      if (options.verbose)
        for {
          _ <- printLine(
            s"> [BLINKY_MUTATION_${mutant.id}=1] " +
              s"""bash -c "sbt $extraSbtParams ${escapeString(options.testCommand)}""""
          )
          _ <- printLine(
            prettyDiff(mutant.diff, mutant.fileName, projectPath.toString, color = true)
          )
          _ <- printLine("--v--" * 5)
        } yield ()
      else
        empty

    val runResult: Instruction[Either[Throwable, TimeoutResult]] =
      prints.flatMap(_ =>
        runBashTimeout(
          s"sbt $extraSbtParams ${escapeString(options.testCommand)}",
          envArgs = defaultEnvArgs + (s"BLINKY_MUTATION_${mutant.id}" -> "1"),
          timeout = (originalTestTime * options.timeoutFactor + options.timeout.toMillis).toLong,
          path = projectPath
        )
      )

    runResult.map {
      case Right(TimeoutResult.Ok)      => RunResult.MutantSurvived
      case Right(TimeoutResult.Timeout) => RunResult.Timeout
      case Left(_)                      => RunResult.MutantKilled
    }
  }

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit] =
    succeed(())

}
