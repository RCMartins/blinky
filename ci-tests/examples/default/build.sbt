lazy val example =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.12.20",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % Test
    )
