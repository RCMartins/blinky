package docs

import java.nio.file.Paths

import blinky.BuildInfo

object Main {

  def main(args: Array[String]): Unit = {
    val settings =
      mdoc
        .MainSettings()
        .withOut(Paths.get("blinky-docs", "target", "docs"))
        .withSiteVariables(
          Map(
            "STABLE_VERSION" -> BuildInfo.stable,
            "SNAPSHOT_VERSION" -> BuildInfo.version,
            "SCALA_VERSION" -> BuildInfo.scalaVersion,
            "SBT_VERSION" -> BuildInfo.sbtVersion
          )
        )
        .withArgs(args.toList)

    val exit = mdoc.Main.process(settings)
    if (exit != 0) sys.exit(exit)
  }
}
