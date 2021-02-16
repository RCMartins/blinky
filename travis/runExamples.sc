import $file.utils, utils._
import ammonite.ops._

import scala.sys.process._

@main
def main(examplesToRun: String*): Unit = {
  println("test")
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