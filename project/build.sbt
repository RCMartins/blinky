lazy val preProcess =
  project
    .in(file("."))
    .settings(
      publish / skip := false,
      moduleName := "pre-process",
      libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1",
      libraryDependencies += "com.lihaoyi"          %% "os-lib"       % "0.9.0"
    )
