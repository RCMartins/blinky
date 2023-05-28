lazy val preProcess =
  project
    .in(file("."))
    .settings(
      publish / skip := false,
      moduleName := "pre-process",
      libraryDependencies ++=
        Seq(
          "com.github.pathikrit" %% "better-files" % "3.9.2",
          "com.lihaoyi"          %% "os-lib"       % "0.8.1"
        ),
    )
