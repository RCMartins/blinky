package blinky.run.external

import os.{Path, ProcessOutput, RelPath}

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success, Try}

object OSExternalCalls extends ExternalCalls {

  def runStream(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      timeout: Option[Long],
      path: Path
  ): Either[Throwable, Unit] =
    Try(
      os.proc((op +: args).map(os.Shellable.StringShellable): _*)
        .call(
          cwd = path,
          stdout = ProcessOutput.Readlines(println),
          stderr = ProcessOutput.Readlines(Console.err.println),
          env = envArgs,
          timeout = timeout.getOrElse(-1L)
        )
    ) match {
      case Failure(exception) => Left(exception)
      case Success(_)         => Right(())
    }

  def runResult(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      timeout: Option[Long],
      path: Path
  ): Either[Throwable, String] =
    Try(
      os.proc((op +: args).map(os.Shellable.StringShellable): _*)
        .call(
          cwd = path,
          env = envArgs,
          timeout = timeout.getOrElse(-1L)
        )
    ) match {
      case Failure(exception) => Left(exception)
      case Success(value)     => Right(value.out.text().trim)
    }

  def makeTemporaryDirectory(): Either[Throwable, Path] =
    Try(os.temp.dir()).toEither

  def makeDirectory(path: Path): Either[Throwable, Unit] =
    Try(os.makeDir(path)).toEither

  def copyInto(from: Path, to: Path): Either[Throwable, Unit] =
    Try(os.copy.into(from, to)).toEither

  def writeFile(filePath: Path, content: String): Either[Throwable, Unit] =
    Try(os.write(filePath, content)).toEither

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

  def copyResource(
      resource: String,
      destinationPath: Path
  ): Either[Throwable, Unit] =
    Try(
      Files.copy(
        getClass.getResource(resource).openStream,
        Paths.get(destinationPath.toString)
      )
    ).toEither.map(_ => ())

  def listFiles(basePath: Path): Either[Throwable, Seq[String]] =
    Try(os.walk(basePath).map(_.toString)).toEither

}
