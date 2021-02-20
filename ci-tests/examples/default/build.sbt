lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.11",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.5" % Test
    )
