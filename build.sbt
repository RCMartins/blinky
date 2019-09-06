import scoverage.ScoverageKeys.coverageFailOnMinimum

lazy val V = _root_.scalafix.sbt.BuildInfo
inThisBuild(
  List(
    organization := "com.github.rcmartins",
    homepage := Some(url("https://github.com/RCMartins/ScalaMutation")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "RCMartins",
        "Ricardo Carvalho Martins",
        "ricardocmartins91@gmail.com",
        url("https://github.com/RCMartins")
      )
    ),
    scalaVersion := V.scala212,
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List(
      "-Yrangepos"
    ),
    classLoaderLayeringStrategy in Compile := ClassLoaderLayeringStrategy.Flat,
    coverageEnabled := false
  )
)

skip in publish := true

lazy val mutators = project.settings(
  moduleName := "MutateCode",
  libraryDependencies += "ch.epfl.scala"     %% "scalafix-core" % V.scalafixVersion,
  libraryDependencies += "com.typesafe.play" %% "play-json"     % "2.7.3",
  coverageMinimum := 81,
  coverageFailOnMinimum := true
)

lazy val input = project.settings(
  skip in publish := true
)

lazy val output = project.settings(
  skip in publish := true
)

lazy val tests = project
  .settings(
    skip in publish := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories :=
      sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      sourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value,
    fork in Test := true
  )
  .dependsOn(mutators)
  .enablePlugins(ScalafixTestkitPlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges
