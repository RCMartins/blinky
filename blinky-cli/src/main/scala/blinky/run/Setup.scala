package blinky.run

import blinky.BuildInfo
import blinky.run.Instruction.{copyResource, runResultSuccess, runStream, succeed}
import os.Path

object Setup {

  def setupCoursier(path: Path): Instruction[String] =
    runResultSuccess("coursier", Seq("--help"), path = path).flatMap {
      case true => succeed("coursier")
      case false =>
        runResultSuccess("cs", Seq("--help"), path = path).flatMap {
          case true  => succeed("cs")
          case false => copyExeFromResources("coursier", path).map(_ => "./coursier")
        }
    }

  def sbtCompileWithSemanticDB(path: Path): Instruction[Unit] =
    // Setup semanticdb files with sbt compile.
    // (there should probably be a better way to do this...)
    runStream(
      "sbt",
      Seq(
        "set Global / semanticdbEnabled := true",
        s"""set Global / semanticdbVersion := "${BuildInfo.semanticdbVersion}"""",
        "compile"
      ),
      envArgs = Map("BLINKY" -> "true"),
      path = path
    ).map(_.toOption.get) // TODO

  def setupScalafix(path: Path): Instruction[Unit] =
    copyExeFromResources("scalafix", path)

  private def copyExeFromResources(name: String, path: Path): Instruction[Unit] =
    for {
      _ <- copyResource(s"/$name", path / name)
      result <- runStream("chmod", Seq("+x", name), path = path)
    } yield result.toOption.get // TODO

}
