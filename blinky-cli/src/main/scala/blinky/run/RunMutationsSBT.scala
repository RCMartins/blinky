package blinky.run

import blinky.run.Instruction._
import blinky.run.Setup.defaultEnvArgs
import blinky.run.Utils._
import os.Path

class RunMutationsSBT(projectPath: Path) extends MutationsRunner {

  private val extraSbtParams: String = ""

  def initializeRunner(): Instruction[Unit] =
    empty

  def initialCompile(compileCommand: String): Instruction[Either[Throwable, Unit]] =
    runResultEither(
      "sbt",
      Seq(extraSbtParams, escapeString(compileCommand)).filter(_.nonEmpty),
      envArgs = defaultEnvArgs,
      path = projectPath
    ).flatMap(either => succeed(either.map(_ => ())))

  def fullTestCommand(testCommand: String): String =
    s"sbt $extraSbtParams ${escapeString(testCommand)}"

  def vanillaTestRun(testCommand: String): RunResultEither[Either[Throwable, String]] =
    runBashEither(
      fullTestCommand(testCommand),
      envArgs = defaultEnvArgs,
      path = projectPath
    )

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit] =
    empty

}
