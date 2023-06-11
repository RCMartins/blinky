package blinky.run

import blinky.run.Instruction._
import blinky.run.Utils._
import os.Path

class RunMutationsBloop(projectPath: Path) extends MutationsRunner {

  def initializeRunner(): Instruction[Unit] =
    runStream(
      "sbt",
      Seq("bloopInstall"),
      envArgs = Setup.defaultEnvArgs,
      path = projectPath
    ).map(_ => ())

  def initialCompile(compileCommand: String): Instruction[Either[Throwable, Unit]] =
    runResultEither(
      "bloop",
      Seq("compile", escapeString(compileCommand)),
      envArgs = Setup.defaultEnvArgs,
      path = projectPath
    ).flatMap(either => succeed(either.map(_ => ())))

  def fullTestCommand(testCommand: String): String =
    s"bloop test ${escapeString(testCommand)}"

  def vanillaTestRun(testCommand: String): RunResultEither[Either[Throwable, String]] =
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
