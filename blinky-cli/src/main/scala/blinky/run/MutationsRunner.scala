package blinky.run

import os.Path

trait MutationsRunner {

  def initializeRunner(projectPath: Path): Instruction[Either[Throwable, Unit]]

  def initialCompile(
      projectPath: Path,
      compileCommand: String
  ): Instruction[Either[Throwable, Unit]]

  def fullTestCommand(testCommand: String): String

  def vanillaTestRun(
      projectPath: Path,
      testCommand: String
  ): Instruction[Either[Throwable, String]]

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit]

}
