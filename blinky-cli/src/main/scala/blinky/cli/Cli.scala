package blinky.cli

import ammonite.ops._
import blinky.run.Run

import scala.util.Try

object Cli {
  def main(args: Array[String]): Unit = {
    args.toSeq match {
      case confPath +: _ => Run.run(Try(Path(confPath)).getOrElse(pwd / RelPath(confPath)))
      case _             => Run.run()
    }
  }
}
