package blinky.run.external

import os.{Path, RelPath}

import scala.util.{Failure, Success, Try}

object OSExternalCalls extends ExternalCalls {

  //TODO: all commands need an error side, Either or Option

  def runSync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Unit =
    os.proc((op +: args).map(os.Shellable.StringShellable): _*)
      .call(cwd = path, env = envArgs)
  // TODO check exit code at least...

  def runSyncEither(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Either[String, String] =
    Try(
      os.proc((op +: args).map(os.Shellable.StringShellable): _*)
        .call(cwd = path, env = envArgs)
    ) match {
      case Failure(exception) =>
        Left(exception.toString)
      case Success(value) =>
        Right(value.out.text().trim)
    }

  def makeTemporaryDirectory(): Path =
    os.temp.dir()

  def makeDirectory(path: Path): Unit =
    os.makeDir(path)

  def copyInto(from: Path, to: Path): Unit =
    os.copy.into(from, to)

  def writeFile(filePath: Path, content: String): Unit =
    os.write(filePath, content)

  def readFile(path: Path): Either[Throwable, String] =
    Try(os.read(path)).toEither

  def isFile(path: Path): Boolean =
    os.isFile(path)

  def copyRelativeFiles(
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path
  ): Either[Throwable, Unit] =
    Try(
      filesToCopy.foreach { fileToCopy =>
        val fromFile = fromPath / fileToCopy
        if (os.exists(fromFile)) {
          os.makeDir.all(toPath / fileToCopy / os.up)
          os.copy.into(fromFile, toPath / fileToCopy / os.up, replaceExisting = true)
        }
      }
    ).toEither

  def listFiles(basePath: Path): Seq[String] =
    Try(os.walk(basePath).map(_.toString)).getOrElse(Seq.empty)

}
