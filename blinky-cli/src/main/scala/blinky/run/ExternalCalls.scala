package blinky.run

import ammonite.ops.Shellable.StringShellable
import ammonite.ops._

object ExternalCalls {

  def runSync(op: String, args: Seq[String])(path: Path): Unit =
    %.applyDynamic(op)(args.map(StringShellable): _*)(path)

  def runAsync(
      op: String,
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty
  )(path: Path): CommandResult =
    Command(Vector.empty, envArgs, Shellout.executeStream)
      .applyDynamic(op)(args.map(StringShellable): _*)(path)

  def runBash(
      args: Seq[String],
      envArgs: Map[String, String] = Map.empty
  )(path: Path): CommandResult =
    runAsync("bash", Seq("-c", args.mkString(" ")), envArgs)(path)

  def makeTemporaryFolder(): Path =
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
