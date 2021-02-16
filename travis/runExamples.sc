import $file.utils, utils._
import ammonite.ops._

import scala.sys.process._

@main
def main(examplesToRun: String*): Unit = {
  println("pos0")
  val basePath = pwd
  val versionNumber = publishLocalBlinky()
  println("versionNumber: " + versionNumber)

  val defaultDirectory = basePath / "examples" / "default"
  val exampleDirectories = ls(basePath / "examples")
}

private def preProcessDirectory(defaultDirectory: Path, testDirectory: Path): Unit = {
  println("pos1")
  Process(
    command = Seq("bash", "-c", s"""cp -nr $defaultDirectory/* $testDirectory"""),
    cwd = pwd.toNIO.toFile
  ).!
  println("pos2")

  val startupScriptName = "startup.sh"
  if (exists(testDirectory / startupScriptName)) {
    %("chmod", "+x", startupScriptName)(testDirectory)
    %(s"./$startupScriptName")(testDirectory)
  }
}