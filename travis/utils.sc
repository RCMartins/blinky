import ammonite.ops._

import scala.sys.process._

def publishLocalBlinky(): String = {
//  val command = %%("sbt", "publishLocal")(pwd)
  val commandStr = Process(command = "sbt publishLocal", cwd = pwd.toNIO.toFile).!!
  val ExtractVersion = "blinky_2\\.12.* :: (\\d+\\.\\d+\\.\\d+[a-zA-Z0-9_\\-+]+) :: ".r
  ExtractVersion.findFirstMatchIn(commandStr).get.group(1)
}
