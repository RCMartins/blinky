import os.{CommandResult, Path, ProcessOutput, copy}

object RunExamples {

  def run(versionNumber: String, args: Array[String]): Unit = {
    val basePath = os.pwd

    val defaultDirectory = basePath / "ci-tests" / "examples" / "default"
    val exampleDirectories = os.list(basePath / "ci-tests" / "examples")

    val examplesToRun: Seq[Path] =
      exampleDirectories.filterNot(_.baseName == "default").filter { examplePath =>
        (args.isEmpty || args.contains(examplePath.baseName)) &&
        !os.exists(examplePath / "disabled")
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
    val tempExamplePath: Path = os.temp.dir()
    copy.into(originalExamplePath, tempExamplePath)
    val testDirectory: Path = tempExamplePath / originalExamplePath.baseName

    def showIfError(result: CommandResult): Unit =
      if (result.exitCode != 0)
        println(result.err.text())

    showIfError(os.proc("git", "init").call(cwd = testDirectory))
    showIfError(
      os.proc("git", "config", "user.email", "you@example.com")
        .call(cwd = testDirectory)
    )
    showIfError(
      os.proc("git", "config", "user.name", "Your Name").call(cwd = testDirectory)
    )
    showIfError(
      os.proc("bash", "-c", s"""cp -nr $defaultDirectory/* $testDirectory""")
        .call(cwd = testDirectory)
    )
    showIfError(
      os.proc("git", "add", ".").call(cwd = testDirectory)
    )
    showIfError(
      os.proc("git", "commit", "-m", "first commit!").call(cwd = testDirectory)
    )

    val startupScript = testDirectory / "startup.sh"
    if (os.exists(startupScript)) {
      println(s"Running $startupScript script...")
      os.proc("chmod", "+x", startupScript).call(cwd = testDirectory)
      os.proc(startupScript).call(cwd = testDirectory)
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
    val msg = s"Testing $examplePath"
    println("-" * msg.length)
    println(msg)
    println("-" * msg.length)

    var outResultText: List[String] = Nil
    val result: CommandResult =
      os
        .proc(
          "cs",
          "launch",
          s"com.github.rcmartins:blinky-cli_2.13:$versionNumber",
          "--",
          "--verbose=true"
        )
        .call(
          cwd = examplePath,
          stdout = ProcessOutput.Readlines { text =>
            outResultText = text :: outResultText
            println(text)
          },
          stderr = ProcessOutput.Readlines(Console.err.println)
        )

    val extraCheckText: String =
      os.read(examplePath / "result.txt")
        .split("\n")
        .map { expectedResult =>
          if (!outResultText.exists(_.contains(expectedResult)))
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
