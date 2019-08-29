import $file.common
import common._
import $file.testMutations
import metaconfig.typesafeconfig._
import ammonite.ops._

import scala.util.Try

val path = pwd

@main
def main(confFilePath: Path = pwd / ".mutations.conf"): Unit = {
  run(MutationsConfig.read(read(confFilePath)))
}

def run(config: MutationsConfig): Unit = {
  val rule = "MutateCode"
  val projectPath = Try(Path(config.projectPath)).getOrElse(pwd / RelPath(config.projectPath))

  val mutatedProjectPath = {
    val tempFolder = tmp.dir()
    val cloneProjectPath = tempFolder / 'project
    println(tempFolder)

    val filesToCopy =
      %%("git", "ls-files", "--others", "--exclude-standard", "--cached")(projectPath).out.string.trim.split("\n").toList
    filesToCopy.foreach {
      fileToCopyStr =>
        val fileToCopy = RelPath(fileToCopyStr)
        mkdir(cloneProjectPath / fileToCopy / up)
        cp.into(projectPath / fileToCopy, cloneProjectPath / fileToCopy / up)
    }

    %('sbt, "compile", RUNNING_MUTATIONS = "true")(cloneProjectPath)

    %("curl", "-Lo", "coursier", "https://git.io/coursier-cli")(cloneProjectPath)
    %("chmod", "+x", "coursier")(cloneProjectPath)

    %(
      "./coursier",
      "bootstrap",
      "ch.epfl.scala:scalafix-cli_2.12.8:0.9.5",
      "-f",
      "--main",
      "scalafix.cli.Cli",
      "-o",
      "scalafix"
    )(cloneProjectPath)

    cloneProjectPath
  }

  val scalafixConfFile = {
    val scalaFixConf = MutateCodeConfig.encoder.write(config.conf).show.trim.stripPrefix("{").stripSuffix("}").trim

    val content =
      s"""MutateCode {
         |  projectPath="$mutatedProjectPath"
         |  $scalaFixConf
         |}
         |""".stripMargin

    val tempFolder = tmp.dir()
    write(tempFolder / ".scalafix.conf", content)
    tempFolder / ".scalafix.conf"
  }

  val fileName = config.filesToMutate

  val semanticDbPath = "target"

  val toolPath = %%(
    "./coursier",
    "fetch",
    "com.github.rcmartins:MutateCode_2.12-0.1.0+8-28cfc07b-SNAPSHOT",
    "-p",
    COURSIER_REPOSITORIES = "ivy2Local|sonatype:snapshots|sonatype:releases"
  )(mutatedProjectPath).out.string.trim

  %(
    "./scalafix",
    "--verbose",
    "--tool-classpath", toolPath,
    "--rules", rule,
    "--files", fileName,
    "--config", scalafixConfFile,
    "--auto-classpath", semanticDbPath,
  )(mutatedProjectPath)

  testMutations.run(mutatedProjectPath, config.sbtCommand, config.options)
}
