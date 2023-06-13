package blinky.run

import blinky.run.Instruction._
import blinky.run.Setup.defaultEnvArgs
import blinky.run.Utils._
import os.Path

object RunMutationsSBT extends MutationsRunner {

  private val extraSbtParams: String = ""

  def initializeRunner(projectPath: Path): Instruction[Either[Throwable, Unit]] =
    succeed(Right(()))

  def initialCompile(
      projectPath: Path,
      compileCommand: String
  ): Instruction[Either[Throwable, Unit]] =
    runResultEither(
      "sbt",
      Seq(extraSbtParams, escapeString(compileCommand)).filter(_.nonEmpty),
      envArgs = defaultEnvArgs,
      path = projectPath
    ).flatMap(either => succeed(either.map(_ => ())))

  def fullTestCommand(testCommand: String): String =
    s"sbt $extraSbtParams ${escapeString(testCommand)}"

  def vanillaTestRun(
      projectPath: Path,
      testCommand: String
  ): Instruction[Either[Throwable, String]] =
    runBashEither(
      fullTestCommand(testCommand),
      envArgs = defaultEnvArgs,
      path = projectPath
    )

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit] =
    empty

}
