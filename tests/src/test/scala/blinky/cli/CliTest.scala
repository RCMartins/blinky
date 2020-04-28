package blinky.cli

import org.scalatest.WordSpec

class CliTest extends WordSpec {
  "Cli --help" should {
    "print the correct help message" in {

      blinky.run.Run.run()
    }
  }
}
