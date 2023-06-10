package blinky.run

import blinky.BuildInfo
import blinky.run.Instruction._
import os.Path

object Setup {

  val defaultEnvArgs: Map[String, String] = Map("BLINKY" -> "true")

  def setupCoursier(path: Path): Instruction[String] =
    runResultEither("coursier", Seq("--help"), path = path).flatMap {
      case Right(_) => succeed("coursier")
      case _ =>
        runResultEither("cs", Seq("--help"), path = path).flatMap {
          case Right(_) => succeed("cs")
          case _        => copyExeFromResources("coursier", path).map(_ => "./coursier")
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
      envArgs = defaultEnvArgs,
      path = path
    ).flatMap {
      case Left(error) =>
        printErrorLine(
          s"""Error compiling with semanticdb enabled!
             |$error
             |""".stripMargin
        )
      case Right(()) =>
        empty
    }

  private def copyExeFromResources(name: String, path: Path): Instruction[Unit] =
    copyResource(s"/$name", path / name)
      .flatMap {
        case Left(error) =>
          printErrorLine(
            s"""Error copying file from resources ($name to $path)
               |$error
               |""".stripMargin
          )
        case Right(()) =>
          runStream("chmod", Seq("+x", name), path = path).flatMap {
            case Left(error) =>
              printErrorLine(
                s"""Error setting file permissions to $path/$name
                   |$error
                   |""".stripMargin
              )
            case Right(()) =>
              empty
          }
      }

}
