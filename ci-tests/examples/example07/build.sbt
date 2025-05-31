lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.20",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % Test,
      scalacOptions += (if (sys.env.contains("BLINKY")) "" else "-Xfatal-warnings")
    )
