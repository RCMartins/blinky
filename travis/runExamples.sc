import $file.utils, utils._
import ammonite.ops._

@main
def main(examplesToRun: String*): Unit = {
  val basePath = pwd
  val versionNumber = publishLocalBlinky()

  val defaultDirectory = basePath / "examples" / "default"
  val exampleDirectories = ls(basePath / "examples")

  val examples: Seq[(Path, CommandResult)] =
    exampleDirectories.filterNot(_.baseName == "default").collect {
      case examplePath if examplesToRun.isEmpty || examplesToRun.contains(examplePath.baseName) =>
        println("\n")
        val msg = s"Testing $examplePath:"
        println("-" * msg.length)
        println(msg)
        println("-" * msg.length)

        preProcessDirectory(defaultDirectory, examplePath)

        val confPath = examplePath / ".blinky.conf"
        val result = %%(
          "cs",
          "launch",
          s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
          "--",
          confPath,
          "--verbose",
          "true"
        )(examplePath)
        println(result.out.string)
        (examplePath, result)
    }

  val brokenExamples = examples.filter(_._2.exitCode != 0)

  if (brokenExamples.nonEmpty) {
    Console.err.println("There were broken tests:")
    println(brokenExamples.map { case (path, _) => s"$path" }.mkString("\n"))
    System.exit(1)
  }
}

private def preProcessDirectory(defaultDirectory: Path, testDirectory: Path): Unit = {
  println("trying to run:")
  println("-" * 40)
  println(s"""cp -nr $defaultDirectory/* $testDirectory""")
  println("-" * 40)

  %("bash", "-c", s"""cp -nr $defaultDirectory/* $testDirectory""")(pwd)

  val startupScriptName = "startup.sh"
  if (exists(testDirectory / startupScriptName)) {
    %("chmod", "+x", startupScriptName)(testDirectory)
    %(s"./$startupScriptName")(testDirectory)
  }
}
