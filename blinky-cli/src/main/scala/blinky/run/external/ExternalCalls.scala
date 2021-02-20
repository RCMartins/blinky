package blinky.run.external

import ammonite.ops.Path

trait ExternalCalls {

  def runSync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Unit

  def runAsync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Either[String, String]

  def makeTemporaryDirectory(): Path

  def makeDirectory(path: Path): Unit

  def copyInto(from: Path, to: Path): Unit

  def writeFile(filePath: Path, content: String): Unit

  def readFile(path: Path): Either[Throwable, String]

  def isFile(path: Path): Boolean

}
