lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.11",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % Test,
      scalacOptions += "-Xfatal-warnings"
    )
