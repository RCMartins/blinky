package blinky.run.external

import os.{Path, RelPath}

trait ExternalCalls {

  def runStream(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      timeout: Option[Long],
      path: Path
  ): Either[Throwable, Unit]

  def runResult(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      timeout: Option[Long],
      path: Path
  ): Either[Throwable, String]

  def makeTemporaryDirectory(): Either[Throwable, Path]

  def makeDirectory(path: Path): Either[Throwable, Unit]

  def copyInto(from: Path, to: Path): Either[Throwable, Unit]

  def writeFile(filePath: Path, content: String): Either[Throwable, Unit]

  def readFile(path: Path): Either[Throwable, String]

  def isFile(path: Path): Boolean

  def copyRelativeFiles(
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path
  ): Either[Throwable, Unit]

  def copyResource(
      resource: String,
      destinationPath: Path
  ): Either[Throwable, Unit]

  def listFiles(basePath: Path): Either[Throwable, Seq[String]]

}
