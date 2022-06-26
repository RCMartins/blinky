package blinky.run.external

import os.{Path, RelPath}

trait ExternalCalls {

  def runSync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Unit

  def runSyncEither(
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

  def copyRelativeFiles(
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path
  ): Either[Throwable, Unit]

  def listFiles(basePath: Path): Seq[String]

}
