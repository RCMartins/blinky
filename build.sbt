import java.io.File

name := "ScalaMutation"

version := "0.1"

// build.sbt
lazy val examples = project.settings(
  scalaVersion := "2.12.8",
  addCompilerPlugin(scalafixSemanticdb), // enable SemanticDB
  scalacOptions ++= List(
    "-Yrangepos",          // required by SemanticDB compiler plugin
    "-Ywarn-unused-import" // required by `RemoveUnused` rule
  )
)

addCommandAlias("fix", "all compile:scalafix test:scalafix")

scalafixConfig := Some(new File(".scalafix.conf"))
