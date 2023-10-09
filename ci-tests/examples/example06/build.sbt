lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.13.12",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % Test
    )
