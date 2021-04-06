lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.13.3",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % Test
    )
