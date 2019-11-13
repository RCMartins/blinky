import ammonite.ops._
import $file.run

val path = pwd

@main
def main(): Unit = {
  val command = %%("sbt", "publishLocal")(path)
  val ExtractVersion = "blinky_2\\.12.* :: (\\d+\\.\\d+\\.\\d+[a-zA-Z0-9_\\-+]+) :: ".r
  val versionNumber = ExtractVersion.findFirstMatchIn(command.out.string).get.group(1)

  val conf = read(path / "travis" / ".mutations.conf")
  val tmpConf = tmp.dir() / ".mutations.conf"
  write(tmpConf, conf + "\nblinkyVersion = \"" + versionNumber + "\"")
  run.main(tmpConf)
}
