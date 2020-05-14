import ammonite.ops._

@main
def main(confPath: Path): Unit = {
  val path = pwd
  val command = %%("sbt", "publishLocal")(path)
  val ExtractVersion = "blinky_2\\.12.* :: (\\d+\\.\\d+\\.\\d+[a-zA-Z0-9_\\-+]+) :: ".r
  val versionNumber = ExtractVersion.findFirstMatchIn(command.out.string).get.group(1)

  %(
    "coursier",
    "launch",
    s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
    "--main",
    "blinky.cli.Cli",
    "--",
    confPath,
    "--blinkyVersion",
    versionNumber
  )(path)
}
