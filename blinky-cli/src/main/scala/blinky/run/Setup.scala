package blinky.run

import ammonite.ops.Path
import ExternalCalls._

import scala.util.Try

object Setup {

  def setupCoursier(path: Path): String =
    if (Try(runAsync("coursier", Seq("--help"))(path)).isSuccess)
      "coursier"
    else if (Try(runAsync("cs", Seq("--help"))(path)).isSuccess)
      "cs"
    else {
      runSync("curl", Seq("-fLo", "cs", "coursier-cli-linux"))(path)
      runSync("chmod", Seq("+x", "cs"))(path)
      "./cs"
    }

  def sbtCompileWithSemanticDB(path: Path): Unit =
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

}
