import ammonite.ops._

def publishLocalBlinky(): String = {
  val path = pwd
  val command = %%("sbt", "publishLocal")(path)
  val ExtractVersion = "blinky_2\\.12.* :: (\\d+\\.\\d+\\.\\d+[a-zA-Z0-9_\\-+]+) :: ".r
  ExtractVersion.findFirstMatchIn(command.out.string).get.group(1)
}
