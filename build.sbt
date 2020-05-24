import scoverage.ScoverageKeys.coverageFailOnMinimum

lazy val V = _root_.scalafix.sbt.BuildInfo
inThisBuild(
  List(
    organization := "com.github.rcmartins",
    homepage := Some(url("https://github.com/rcmartins/blinky")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "rcmartins",
        "Ricardo Carvalho Martins",
        "ricardocmartins91@gmail.com",
        url("https://github.com/rcmartins")
      )
    ),
    scalaVersion := V.scala212,
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List(
      "-Yrangepos",
      "-deprecation"
    ),
    coverageEnabled := false,
    fork in Test := true
  )
)

skip in publish := true

lazy val core =
  project
    .in(file("blinky-core"))
    .enablePlugins(BuildInfoPlugin)
    .settings(
      moduleName := "blinky",
      libraryDependencies += "ch.epfl.scala"        %% "scalafix-core" % V.scalafixVersion,
      libraryDependencies += "com.typesafe.play"    %% "play-json"     % "2.8.1",
      libraryDependencies += "com.github.pathikrit" %% "better-files"  % "3.9.1",
      libraryDependencies += "com.lihaoyi"          %% "ammonite-ops"  % "2.1.4",
      coverageMinimum := 81,
      coverageFailOnMinimum := true,
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "blinky"
    )

lazy val input = project.settings(
  skip in publish := true
)

lazy val output = project.settings(
  skip in publish := true
)

lazy val cli =
  project
    .in(file("blinky-cli"))
    .settings(
      moduleName := "blinky-cli",
      libraryDependencies += "com.geirsson"     %% "metaconfig-core"            % "0.9.10",
      libraryDependencies += "com.geirsson"     %% "metaconfig-typesafe-config" % "0.9.10",
      libraryDependencies += "com.github.scopt" %% "scopt"                      % "4.0.0-RC2",
      libraryDependencies += "org.scalatest"    %% "scalatest"                  % "3.1.2" % "test",
      coverageMinimum := 20,
      coverageFailOnMinimum := true
    )
    .dependsOn(core)

lazy val tests = project
  .settings(
    skip in publish := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories :=
      sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      sourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value
  )
  .dependsOn(core, cli)
  .enablePlugins(ScalafixTestkitPlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges
