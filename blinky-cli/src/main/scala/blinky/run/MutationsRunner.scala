package blinky.run

import blinky.run.Instruction._
import os.Path

trait MutationsRunner {

  def initializeRunner: Instruction[Either[Throwable, Unit]]

  def initialCompile(compileCommand: String): Instruction[Either[Throwable, Unit]]

  def fullTestCommand(testCommand: String): String

  def vanillaTestRun(testCommand: String): RunResultEither[Either[Throwable, String]]

  def cleanRunnerAfter(projectPath: Path, results: List[(Int, RunResult)]): Instruction[Unit]

}
