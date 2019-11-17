import ammonite.ops._

val path = pwd

@main
def main(): Unit = {
  val command = %%("sbt", "publishLocal")(path)
  val ExtractVersion = "blinky_2\\.12.* :: (\\d+\\.\\d+\\.\\d+[a-zA-Z0-9_\\-+]+) :: ".r
  val versionNumber = ExtractVersion.findFirstMatchIn(command.out.string).get.group(1)

  val conf = read(path / "travis" / ".blinky.conf")
  val tmpConf = tmp.dir() / ".blinky.conf"
  write(tmpConf, conf + "\nblinkyVersion = \"" + versionNumber + "\"")

  %(
    "./coursier",
    'launch,
    s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
    "--main",
    "blinky.cli.Cli"
  )(path)
}
