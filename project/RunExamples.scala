import ammonite.ops._
import os.copy

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

    val examplesResult: Seq[(Path, CommandResult, String)] =
      examplesToRun.filterNot(_.baseName == "default").map { examplePath =>
        runExample(versionNumber, defaultDirectory, examplePath)
      }

    val brokenExamples = examplesResult.filter(_._2.exitCode != 0)

    if (brokenExamples.nonEmpty) {
      Console.err.println("There were broken tests:")
      println(
        brokenExamples.map { case (path, _, extraText) => s"$path\n$extraText" }.mkString("\n")
      )
      System.exit(1)
    } else
      println("All tests were successful!")
  }

  private def preProcessDirectory(
      defaultDirectory: Path,
      originalExamplePath: Path
  ): Path = {
    val tempExamplePath: Path = tmp.dir()
    copy.into(originalExamplePath, tempExamplePath)
    val testDirectory: Path = tempExamplePath / originalExamplePath.baseName

    def showIfError(result: CommandResult): Unit =
      if (result.exitCode != 0)
        println(result.err.string)

    showIfError(%%("git", "init")(testDirectory))
    showIfError(%%("bash", "-c", s"""cp -nr $defaultDirectory/* $testDirectory""")(testDirectory))
    showIfError(%%("git", "add", ".")(testDirectory))
    showIfError(%%("git", "commit", "-m", "first commit!", "--author", "ci-test")(testDirectory))

    val startupScript = testDirectory / "startup.sh"
    if (exists(startupScript)) {
      println(s"Running $startupScript script...")
      %("chmod", "+x", startupScript)(testDirectory)
      %(startupScript)(testDirectory)
    }

    testDirectory
  }

  private def runExample(
      versionNumber: String,
      defaultDirectory: Path,
      originalExamplePath: Path
  ): (Path, CommandResult, String) = {
    val examplePath: Path =
      preProcessDirectory(defaultDirectory, originalExamplePath)

    println("\n")
    val msg = s"Testing $examplePath:"
    println("-" * msg.length)
    println(msg)
    println("-" * msg.length)

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

    val extraCheckText: String =
      read(examplePath / "result.txt")
        .split("\n")
        .map { expectedResult =>
          if (!result.out.string.contains(expectedResult))
            s"""${"-" * 10}
               |Example failed. Expected line does not appear:
               |$expectedResult
               |${"-" * 10}
               |""".stripMargin
          else
            ""
        }
        .mkString("\n")

    val resultUpdated =
      if (extraCheckText.nonEmpty)
        result.copy(exitCode = 1)
      else
        result

    (examplePath, resultUpdated, extraCheckText)
  }

}
