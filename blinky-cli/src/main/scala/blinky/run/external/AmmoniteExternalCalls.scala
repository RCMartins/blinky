package blinky.run.external

import ammonite.ops.Shellable.StringShellable
import ammonite.ops._

import scala.util.{Failure, Success, Try}

object AmmoniteExternalCalls extends ExternalCalls {

  //TODO: all commands need an error side, Either or Option

  def runSync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Unit =
    Command(Vector.empty, envArgs, Shellout.executeInteractive)
      .applyDynamic(op)(args.map(StringShellable): _*)(path)

  def runAsync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Either[String, String] =
    Try(
      Command(Vector.empty, envArgs, Shellout.executeStream)
        .applyDynamic(op)(args.map(StringShellable): _*)(path)
    ) match {
      case Failure(exception) =>
        Left(exception.toString)
      case Success(value) =>
        Right(value.out.string.trim)
    }

  def makeTemporaryDirectory(): Path =
    tmp.dir()

  def makeDirectory(path: Path): Unit =
    mkdir(path)

  def copyInto(from: Path, to: Path): Unit =
    cp.into(from, to)

  def writeFile(filePath: Path, content: String): Unit =
    write(filePath, content)

  def readFile(path: Path): Either[Throwable, String] =
    Try(read(path)).toEither

  def isFile(path: Path): Boolean =
    path.isFile

  def copyRelativeFiles(
      filesToCopy: Seq[RelPath],
      fromPath: Path,
      toPath: Path
  ): Either[Throwable, Unit] =
    Try(
      filesToCopy.foreach { fileToCopy =>
        val fromFile = fromPath / fileToCopy
        if (exists(fromFile)) {
          mkdir(toPath / fileToCopy / up)
          cp.into(fromFile, toPath / fileToCopy / up)
        }
      }
    ).toEither

  def lsFiles(basePath: Path): Seq[String] =
    Try(ls.rec(basePath).map(_.toString)).getOrElse(Seq.empty)

}
