package blinky.run

import blinky.internal.MutantFile
import blinky.run.Instruction._
import blinky.run.Utils._
import blinky.run.config.OptionsConfig
import os.Path

class TestMutationsBloop(projectPath: Path) extends TestMutationsRunner {

  def initializeRunner(): Instruction[Unit] =
    runStream(
      "sbt",
      Seq("bloopInstall"),
      envArgs = defaultEnvArgs,
      path = projectPath
    ).map(_ => ())

  def initialCompile(compileCommand: String): RunResultEither[Either[Throwable, String]] =
    runResultEither(
      "bloop",
      Seq("compile", escapeString(compileCommand)),
      envArgs = defaultEnvArgs,
      path = projectPath
    )

  def vanillaTestRun(testCommand: String): RunResultEither[Either[Throwable, String]] =
    runBashEither(
      s"bloop test ${escapeString(testCommand)}",
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
              s"""bash -c "bloop test ${escapeString(options.testCommand)}""""
          )
          _ <- printLine(
            prettyDiff(mutant.diff, mutant.fileName, projectPath.toString, color = true)
          )
          _ <- printLine("--v--" * 5)
        } yield ()
      else
        empty

    val runBloopResult: Instruction[Either[Throwable, TimeoutResult]] =
      prints.flatMap(_ =>
        runBashTimeout(
          s"bloop test ${escapeString(options.testCommand)}",
          envArgs = Map(
            "BLINKY" -> "true",
            s"BLINKY_MUTATION_${mutant.id}" -> "1"
          ),
          timeout = (originalTestTime * options.timeoutFactor + options.timeout.toMillis).toLong,
          path = projectPath
        )
      )

    runBloopResult.map {
      case Right(TimeoutResult.Ok)      => RunResult.MutantSurvived
      case Right(TimeoutResult.Timeout) => RunResult.Timeout
      case Left(_)                      => RunResult.MutantKilled
    }
  }

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit] =
    if (results.exists(_._2 == RunResult.Timeout))
      runBashEither("bloop exit", path = projectPath).flatMap {
        case Left(error) =>
          printErrorLine(
            s"""Failed to run 'bloop exit' after a mutation timeout.
               |$error""".stripMargin
          )
        case Right(_) =>
          succeed(())
      }
    else
      succeed(())

}
