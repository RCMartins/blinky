package docs

import better.files.File

import java.nio.file.Paths
import blinky.BuildInfo

object Main {

  def main(args: Array[String]): Unit = {
    val ExtractVersion = """.*scalaVersion := "(.*)".*""".r
    val scala212Version: String =
      File("ci-tests/examples/default/build.sbt").lines
        .collectFirst { case ExtractVersion(version) => version }
        .getOrElse("Failed to extract Scala 2.12 version from ci-tests/examples/default/build.sbt")

    val settings =
      mdoc
        .MainSettings()
        .withOut(Paths.get("blinky-docs", "target", "docs"))
        .withSiteVariables(
          Map(
            "STABLE_VERSION" -> BuildInfo.stable,
            "SNAPSHOT_VERSION" -> BuildInfo.version,
            "SCALA_VERSION_212" -> scala212Version,
            "SCALA_VERSION_213" -> BuildInfo.scalaVersion,
            "SBT_VERSION" -> BuildInfo.sbtVersion
          )
        )
        .withArgs(args.toList)

    val exit = mdoc.Main.process(settings)
    if (exit != 0) sys.exit(exit)
  }
}
