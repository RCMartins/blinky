package blinky.run.external

import ammonite.ops.{CommandResult, Path}

trait ExternalCalls {

  def runSync(op: String, args: Seq[String], path: Path): Unit

  def runAsync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty,
      path: Path
  ): CommandResult

  def makeTemporaryDirectory(): Path

  def makeDirectory(path: Path): Unit

  def copyInto(from: Path, to: Path): Unit

  def writeFile(filePath: Path, content: String): Unit

  def readFile(path: Path): String

  def isFile(path: Path): Boolean

}
