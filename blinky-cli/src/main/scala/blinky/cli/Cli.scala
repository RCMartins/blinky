package blinky.cli

import blinky.run.Run
import os.Path

object Cli {
  def main(args: Array[String]): Unit = {
    args match {
      case confPath +: _ => Run.run(Path(confPath))
      case _             => Run.run()
    }
  }
}
