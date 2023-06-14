package blinky.run

import blinky.run.Instruction._
import blinky.run.Utils._
import os.Path

object RunMutationsBloop extends MutationsRunner {

  def initializeRunner(projectPath: Path): Instruction[Either[Throwable, Unit]] =
    runStream(
      "sbt",
      Seq("bloopInstall"),
      envArgs = Setup.defaultEnvArgs,
      path = projectPath
    )

  def initialCompile(
      projectPath: Path,
      compileCommand: String
  ): Instruction[Either[Throwable, Unit]] =
    runResultEither(
      "bloop",
      Seq("compile", escapeString(compileCommand)),
      envArgs = Setup.defaultEnvArgs,
      path = projectPath
    ).flatMap(either => succeed(either.map(_ => ())))

  def fullTestCommand(testCommand: String): String =
    s"bloop test ${escapeString(testCommand)}"

  def vanillaTestRun(
      projectPath: Path,
      testCommand: String
  ): Instruction[Either[Throwable, String]] =
    runBashEither(
      fullTestCommand(testCommand),
      envArgs = Setup.defaultEnvArgs,
      path = projectPath
    )

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit] =
    when(results.exists(_._2 == RunResult.Timeout))(
      runBashEither("bloop exit", path = projectPath).flatMap {
        case Left(error) =>
          printErrorLine(
            s"""Failed to run 'bloop exit' after a mutation timeout.
               |$error""".stripMargin
          )
        case Right(_) =>
          empty
      }
    )

}
