import java.nio.file.Path

import sbt.Keys._
import sbt.nio.file.FileAttributes
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
      "-encoding",
      "UTF-8",
      "-explaintypes",
      "-unchecked",
      "-feature",
      "-deprecation:true",
      "-Xfuture",
      "-Xcheckinit",
      "-Xlint:by-name-right-associative",
      "-Xlint:constant",
      "-Xlint:delayedinit-select",
      "-Xlint:doc-detached",
      "-Xlint:missing-interpolator",
      "-Xlint:option-implicit",
      "-Xlint:package-object-classes",
      "-Xlint:poly-implicit-overload",
      "-Xlint:private-shadow",
      "-Xlint:stars-align",
      "-Xlint:type-parameter-shadow",
      "-Xlint:unsound-match",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-infer-any",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-Ypartial-unification",
      "-Yno-adapted-args",
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:params",
      "-Ywarn-macros:after",
      "-Yrangepos",
      if (sys.env.contains("CI")) "-Xfatal-warnings" else ""
    ),
    coverageEnabled := false,
    fork in Test := false,
    skip in publish := true
  )
)

Global / excludeFilter := NothingFilter
Global / fileInputExcludeFilter := ((_: Path, _: FileAttributes) => false)
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val stableVersion = Def.setting {
  version.in(ThisBuild).value.replaceAll("\\+.*", "")
}

lazy val core =
  project
    .in(file("blinky-core"))
    .enablePlugins(BuildInfoPlugin)
    .settings(
      skip in publish := false,
      moduleName := "blinky",
      libraryDependencies += "ch.epfl.scala"        %% "scalafix-core" % V.scalafixVersion,
      libraryDependencies += "com.typesafe.play"    %% "play-json"     % "2.8.1",
      libraryDependencies += "com.github.pathikrit" %% "better-files"  % "3.9.1",
      libraryDependencies += "com.lihaoyi"          %% "ammonite-ops"  % "2.1.4",
      libraryDependencies += "org.scalatest"        %% "scalatest"     % "3.1.2" % "test",
      coverageMinimum := 89,
      coverageFailOnMinimum := true,
      buildInfoPackage := "blinky",
      buildInfoKeys := Seq[BuildInfoKey](
        version,
        "stable" -> stableVersion.value,
        scalaVersion,
        sbtVersion
      )
    )

lazy val input =
  project
    .settings(
      scalacOptions := Seq.empty
    )

lazy val output =
  project
    .settings(
      scalacOptions := Seq.empty
    )

lazy val cli =
  project
    .in(file("blinky-cli"))
    .settings(
      skip in publish := false,
      moduleName := "blinky-cli",
      libraryDependencies += "com.geirsson"               %% "metaconfig-core"            % "0.9.10",
      libraryDependencies += "com.geirsson"               %% "metaconfig-typesafe-config" % "0.9.10",
      libraryDependencies += "com.github.scopt"           %% "scopt"                      % "4.0.0-RC2",
      libraryDependencies += "com.softwaremill.quicklens" %% "quicklens"                  % "1.5.0",
      libraryDependencies += "org.scalatest"              %% "scalatest"                  % "3.1.2" % "test",
      Test / scalacOptions -= "-Ywarn-unused:locals",
      coverageMinimum := 43,
      coverageFailOnMinimum := true
    )
    .dependsOn(core)

lazy val tests =
  project
    .enablePlugins(ScalafixTestkitPlugin)
    .settings(
      libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
      scalafixTestkitOutputSourceDirectories :=
        sourceDirectories.in(output, Compile).value,
      scalafixTestkitInputSourceDirectories :=
        sourceDirectories.in(input, Compile).value,
      scalafixTestkitInputClasspath :=
        fullClasspath.in(input, Compile).value
    )
    .dependsOn(core, cli)

lazy val docs =
  project
    .in(file("blinky-docs"))
    .enablePlugins(MdocPlugin, DocusaurusPlugin)
    .settings(
      mdoc := run.in(Compile).evaluated
    )
    .dependsOn(core)
