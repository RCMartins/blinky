import ammonite.ops._

import scala.sys.process._

def publishLocalBlinky(): String = {
//  val command = %%("sbt", "publishLocal")(pwd)
  println("bla0")
  val commandStr = Process(command = "sbt publishLocal", cwd = pwd.toNIO.toFile).!!
  println("bla1")
  println(commandStr.take(100))
  println("bla2")
  println(commandStr.take(500))
  println("bla3")
  println(commandStr)
  println("bla4")
  val ExtractVersion = "blinky_2\\.12.* :: (\\d+\\.\\d+\\.\\d+[a-zA-Z0-9_\\-+]+) :: ".r
  ExtractVersion.findFirstMatchIn(commandStr).get.group(1)
}
