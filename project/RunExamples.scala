import scala.sys.process._

import ammonite.ops._

object RunExamples {

  def run(versionNumber: String, args: Array[String]): Unit = {
    val basePath = pwd

    val defaultDirectory = basePath / "ci-tests" / "examples" / "default"
    val exampleDirectories = ls(basePath / "ci-tests" / "examples")

    val examplesToRun: Seq[Path] =
      exampleDirectories.filterNot(_.baseName == "default").filter { examplePath =>
        (args.isEmpty || args.contains(examplePath.baseName)) &&
        !(exists ! (examplePath / "disabled"))
      }

    if (examplesToRun.isEmpty) {
      Console.err.println("No example tests found.")
      System.exit(1)
    }

    val examples: Seq[(Path, CommandResult)] =
      examplesToRun.filterNot(_.baseName == "default").map { examplePath =>
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
          s"com.github.rcmartins:blinky-cli_2.13:$versionNumber",
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
    } else
      println("All tests were successful!")
  }

  private def preProcessDirectory(defaultDirectory: Path, testDirectory: Path): Unit = {
    Process(
      command = Seq("bash", "-c", s"""cp -nr $defaultDirectory/* $testDirectory"""),
      cwd = pwd.toNIO.toFile
    ).!

    val startupScriptName = "startup.sh"
    if (exists(testDirectory / startupScriptName)) {
      %("chmod", "+x", startupScriptName)(testDirectory)
      %(s"./$startupScriptName")(testDirectory)
    }
  }
}
