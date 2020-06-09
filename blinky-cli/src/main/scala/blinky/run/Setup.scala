package blinky.run

import ammonite.ops.Path
import blinky.run.Instruction.{CopyResource, runAsyncSuccess, runSync, succeed}

object Setup {

  def setupCoursier(path: Path): Instruction[String] =
    runAsyncSuccess("coursier", Seq("--help"))(path).flatMap {
      case true => succeed("coursier")
      case false =>
        runAsyncSuccess("cs", Seq("--help"))(path).flatMap {
          case true  => succeed("cs")
          case false => copyExeFromResources("coursier", path).map(_ => "./coursier")
        }
    }

  def sbtCompileWithSemanticDB(path: Path): Instruction[Unit] =
    // Setup semanticdb files with sbt compile.
    // (there should probably be a better way to do this...)
    runSync(
      "sbt",
      Seq(
        "set ThisBuild / semanticdbEnabled := true",
        "set ThisBuild / semanticdbVersion := \"4.3.12\"",
        "compile"
      )
    )(path)

  def setupScalafix(path: Path): Instruction[Unit] =
    copyExeFromResources("scalafix", path)

  private def copyExeFromResources(name: String, path: Path): Instruction[Unit] =
    CopyResource(
      s"/$name",
      path / name,
      runSync("chmod", Seq("+x", name))(path)
    )

}
