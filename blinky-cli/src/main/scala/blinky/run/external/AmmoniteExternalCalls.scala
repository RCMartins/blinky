package blinky.run.external

import ammonite.ops.Shellable.StringShellable
import ammonite.ops._

object AmmoniteExternalCalls extends ExternalCalls {

  def runSync(op: String, args: Seq[String])(path: Path): Unit =
    %.applyDynamic(op)(args.map(StringShellable): _*)(path)

  def runAsync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty
  )(path: Path): CommandResult =
    Command(Vector.empty, envArgs, Shellout.executeStream)
      .applyDynamic(op)(args.map(StringShellable): _*)(path)

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

}
