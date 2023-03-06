import java.nio.file.Path
import sbt.Keys._
import sbt.nio.file.FileAttributes
import sbt.util.FileInfo
import scoverage.ScoverageKeys.coverageFailOnMinimum
import complete.DefaultParsers._

val semanticdbScalac = "4.5.9"

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
    scalaVersion := V.scala213,
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
                      else "-Werror"),
    coverageEnabled := false,
    Test / fork := false,
    publish / skip := true
  )
)

Global / excludeFilter := NothingFilter
Global / fileInputExcludeFilter := ((_: Path, _: FileAttributes) => false)
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val stableVersion = Def.setting {
  (ThisBuild / version).value.replaceAll("\\+.*", "")
}

lazy val buildInfoSettings: Seq[Def.Setting[_]] =
  Seq(
    buildInfoPackage := "blinky",
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      "stable" -> stableVersion.value,
      scalaVersion,
      "scalaMinorVersion" -> scalaVersion.value.take(4),
      sbtVersion,
      "semanticdbVersion" -> semanticdbScalac
    )
  )

lazy val core =
  project
    .in(file("blinky-core"))
    .settings(
      publish / skip := false,
      moduleName := "blinky",
      libraryDependencies += "ch.epfl.scala"        %% "scalafix-core" % V.scalafixVersion,
      libraryDependencies += "com.typesafe.play"    %% "play-json"     % "2.9.2",
      libraryDependencies += "com.github.pathikrit" %% "better-files"  % "3.9.2",
      libraryDependencies += "com.lihaoyi"          %% "os-lib"        % "0.8.1",
      libraryDependencies += "org.scalatest"        %% "scalatest"     % "3.2.13" % "test",
      coverageMinimumStmtTotal := 94,
      coverageFailOnMinimum := true,
      buildInfoSettings
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
        val generatedFolder = (Compile / sourceManaged).value

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
    .enablePlugins(BuildInfoPlugin)
    .settings(
      publish / skip := false,
      moduleName := "blinky-cli",
      libraryDependencies += "com.softwaremill.quicklens" %% "quicklens" % "1.9.0",
      libraryDependencies += "com.geirsson"     %% "metaconfig-typesafe-config" % "0.9.11",
      libraryDependencies += "com.geirsson"     %% "metaconfig-core"            % "0.9.11",
      libraryDependencies += "com.github.scopt" %% "scopt"                      % "4.1.0",
      libraryDependencies += "dev.zio"          %% "zio"                        % "2.0.10",
      libraryDependencies += "dev.zio"          %% "zio-test"                   % "2.0.10" % "test",
      libraryDependencies += "dev.zio"          %% "zio-test-sbt"               % "2.0.10" % "test",
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      Test / scalacOptions -= "-Ywarn-unused:locals",
      coverageMinimumStmtTotal := 30,
      coverageFailOnMinimum := true
    )
    .settings(buildInfoSettings)
    .dependsOn(core)

lazy val tests =
  project
    .enablePlugins(ScalafixTestkitPlugin)
    .settings(
      libraryDependencies += "ch.epfl.scala"             % "scalafix-testkit" %
        SBTDefaults.scalafixTestkitV(scalaVersion.value) % Test cross CrossVersion.full,
      libraryDependencies += "org.scalatest"            %% "scalatest"        % "3.2.13" % Test,
      scalafixTestkitOutputSourceDirectories :=
        (output / Compile / sourceDirectories).value,
      scalafixTestkitInputSourceDirectories :=
        (input / Compile / sourceDirectories).value,
      scalafixTestkitInputClasspath :=
        (input / Compile / fullClasspath).value
    )
    .dependsOn(core, output)

lazy val docs =
  project
    .in(file("blinky-docs"))
    .enablePlugins(BuildInfoPlugin, MdocPlugin, DocusaurusPlugin)
    .settings(
      mdoc := (Compile / run).evaluated
    )
    .settings(buildInfoSettings)
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

Global / excludeLintKeys += core / buildInfoPackage
Global / excludeLintKeys += core / buildInfoKeys
