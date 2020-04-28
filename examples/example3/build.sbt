lazy val example3 =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.10",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
