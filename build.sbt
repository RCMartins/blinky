import SBTDefaults.scalafixTestkitV

import java.nio.file.Path
import sbt.Keys._
import sbt.nio.file.FileAttributes
import sbt.util.FileInfo
import scoverage.ScoverageKeys.coverageFailOnMinimum
import complete.DefaultParsers._

val semanticdbScalac = "4.4.16"

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
    // addCompilerPlugin(scalafixSemanticdb),
    addCompilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % semanticdbScalac cross CrossVersion.full
    ),
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("2.13."))
        SBTDefaults.defaultScalacFlags213
      else
        SBTDefaults.defaultScalacFlags212
    },
    scalacOptions -= (if (sys.env.contains("CI") && !sys.env.contains("BLINKY")) ""
                      else "-Xfatal-warnings"),
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
      libraryDependencies += "com.typesafe.play"    %% "play-json"     % "2.9.2",
      libraryDependencies += "com.github.pathikrit" %% "better-files"  % "3.9.1",
      libraryDependencies += "com.lihaoyi"          %% "ammonite-ops"  % "2.3.8",
      libraryDependencies += "org.scalatest"        %% "scalatest"     % "3.2.8" % "test",
      coverageMinimum := 94,
      coverageFailOnMinimum := true,
      buildInfoPackage := "blinky",
      buildInfoKeys := Seq[BuildInfoKey](
        version,
        "stable" -> stableVersion.value,
        scalaVersion,
        sbtVersion,
        "semanticdbVersion" -> semanticdbScalac
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
      scalacOptions := Seq.empty,
      Compile / sourceGenerators += Def.task {
        val sourcesFolder = file((Compile / scalaSource).value.toString + "-output")
        val generatedFolder = (sourceManaged in Compile).value

        val cachedFunc =
          FileFunction.cached(
            file("output/.blinky-cache"),
            FileInfo.full
          ) { files =>
            files.flatMap(PreProcess.preProcessOutputFiles(_, generatedFolder))
          }
        cachedFunc(Set(sourcesFolder, file("project/PreProcess.scala"))).toSeq
      }.taskValue
    )

lazy val cli =
  project
    .in(file("blinky-cli"))
    .settings(
      skip in publish := false,
      moduleName := "blinky-cli",
      libraryDependencies += "com.geirsson"               %% "metaconfig-core"            % "0.9.11",
      libraryDependencies += "com.geirsson"               %% "metaconfig-typesafe-config" % "0.9.11",
      libraryDependencies += "com.github.scopt"           %% "scopt"                      % "4.0.1",
      libraryDependencies += "com.softwaremill.quicklens" %% "quicklens"                  % "1.7.1",
      libraryDependencies += "dev.zio"                    %% "zio"                        % "1.0.7",
      libraryDependencies += "dev.zio"                    %% "zio-test"                   % "1.0.7" % "test",
      libraryDependencies += "dev.zio"                    %% "zio-test-sbt"               % "1.0.7" % "test",
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      Test / scalacOptions -= "-Ywarn-unused:locals",
      coverageMinimum := 30,
      coverageFailOnMinimum := true
    )
    .dependsOn(core)

lazy val tests =
  project
    .enablePlugins(ScalafixTestkitPlugin)
    .settings(
      libraryDependencies += "ch.epfl.scala"  % "scalafix-testkit" %
        scalafixTestkitV(scalaVersion.value)  % Test cross CrossVersion.full,
      libraryDependencies += "org.scalatest" %% "scalatest"        % "3.2.8" % Test,
      scalafixTestkitOutputSourceDirectories :=
        sourceDirectories.in(output, Compile).value,
      scalafixTestkitInputSourceDirectories :=
        sourceDirectories.in(input, Compile).value,
      scalafixTestkitInputClasspath :=
        fullClasspath.in(input, Compile).value
    )
    .dependsOn(core, output)

lazy val docs =
  project
    .in(file("blinky-docs"))
    .enablePlugins(MdocPlugin, DocusaurusPlugin)
    .settings(
      mdoc := run.in(Compile).evaluated
    )
    .dependsOn(core)

lazy val runCurrent =
  inputKey[Unit]("Run current blinky version on itself")
lazy val runExamples =
  inputKey[Unit]("Run example projects to test blinky")
lazy val runCommunityProjects =
  inputKey[Unit]("Run community scala projects to test blinky regressions")

runCurrent := {
  val a = (core / publishLocal).value
  val b = (cli / publishLocal).value
  val args: Array[String] = spaceDelimited("<arg>").parsed.toArray
  RunCurrentVersion.run(version.value, args)
}

runExamples := {
  val a = (core / publishLocal).value
  val b = (cli / publishLocal).value
  val args: Array[String] = spaceDelimited("<arg>").parsed.toArray
  RunExamples.run(version.value, args)
}

runCommunityProjects := {
  val a = (core / publishLocal).value
  val b = (cli / publishLocal).value
  val args: Array[String] = spaceDelimited("<arg>").parsed.toArray
  RunCommunityProjects.run(version.value, args)
}
