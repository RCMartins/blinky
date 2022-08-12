package blinky.run

import blinky.run.Instruction._
import blinky.run.config.OptionsConfig
import os.Path

trait TestMutationsRunner {

  def initializeRunner(): Instruction[Unit]

  def initialCompile(compileCommand: String): RunResultEither[Either[Throwable, String]]

  def vanillaTestRun(testCommand: String): RunResultEither[Either[Throwable, String]]

  def runMutant(
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutant: Mutant
  ): Instruction[RunResult]

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit]

}
