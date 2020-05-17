import ammonite.ops._

@main
def main(): Unit = {
  val basePath = pwd
  val versionNumber = publishLocalBlinky()

  val exampleDirectories = ls(basePath / "examples")

  val examples: Seq[(Path, CommandResult)] =
    exampleDirectories.map {
      examplePath =>
        println("\n")
        println("-" * 60)
        println(s"Testing $examplePath:")
        println("-" * 60)
        val confPath = examplePath / ".blinky.conf"
        val result = %%(
          "coursier",
          "launch",
          s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
          "--main",
          "blinky.cli.Cli",
          "--",
          confPath,
          "--blinkyVersion",
          versionNumber
        )(examplePath)
        println(result.out.string)
        (examplePath, result)
    }

  val brokenExamples = examples.filter(_._2.exitCode != 0)

  if (brokenExamples.nonEmpty) {
    println("There were broken tests:")
    println(brokenExamples.map { case (path, _) => s"$path" }.mkString("\n"))
    System.exit(1)
  }
}
