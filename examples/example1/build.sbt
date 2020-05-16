lazy val example1 =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.11",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
