package blinky.run.external

import ammonite.ops.Shellable.StringShellable
import ammonite.ops._

import scala.util.{Failure, Success, Try}
import scala.sys.process._

object AmmoniteExternalCalls extends ExternalCalls {

  def runSync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String],
      path: Path
  ): Unit = {
    println("#" * 80)
    println(s"{{{path: $path}}}")
    println(s"Executing ${envArgs.mkString("[", ", ", "]")} $op(${args.mkString(", ")})")
    println("#" * 80)
//    Command(Vector.empty, envArgs, Shellout.executeInteractive)
//      .applyDynamic(op)(args.map(StringShellable): _*)(path)

    Process(op +: args, cwd = path.toNIO.toFile, extraEnv = envArgs.toVector: _*).!
  }

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

  def readFile(path: Path): String =
    read(path)

  def isFile(path: Path): Boolean =
    path.isFile

}
