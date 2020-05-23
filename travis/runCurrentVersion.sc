import $file.utils, utils._
import ammonite.ops._

@main
def main(confPath: Path): Unit = {
  val path = pwd
  val versionNumber = publishLocalBlinky()

  %(
    "cs",
    "launch",
    s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
    "--main",
    "blinky.cli.Cli",
    "--",
    confPath
  )(path)
}
