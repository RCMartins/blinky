import ammonite.ops._

@main
def main(): Unit = {
  val basePath = pwd
  val command = %%("sbt", "publishLocal")(basePath)
  val ExtractVersion = "blinky_2\\.12.* :: (\\d+\\.\\d+\\.\\d+[a-zA-Z0-9_\\-+]+) :: ".r
  val versionNumber = ExtractVersion.findFirstMatchIn(command.out.string).get.group(1)

  val exampleDirectories = ls(basePath / "examples")

  exampleDirectories.foreach {
    examplePath =>
      println(s"Testing $examplePath:")
      val confPath = examplePath / ".blinky.conf"
      %(
        s"$basePath/bin/coursier",
        "launch",
        s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
        "--main",
        "blinky.cli.Cli",
        "--",
        confPath,
        "--blinkyVersion",
        versionNumber
      )(examplePath)
  }
}
