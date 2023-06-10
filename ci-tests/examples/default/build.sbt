lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.18",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % Test
    )
