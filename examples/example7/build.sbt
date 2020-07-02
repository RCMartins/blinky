lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.11",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % Test,
      scalacOptions += (if (sys.env.contains("BLINKY")) "" else "-Xfatal-warnings")
    )
