package blinky.cli

import java.io.{ByteArrayOutputStream, PrintStream}

import blinky.BuildInfo.version
import org.scalatest.{MustMatchers, WordSpec}

class CliTest extends WordSpec with MustMatchers {
  "Cli --version" should {
    "print the correct version" in {
      val systemOut = System.out

      val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
      val ps: PrintStream = new PrintStream(baos)

      println(ammonite.ops.%%("pwd")(ammonite.ops.pwd).out.string)
      Console.setOut(ps)
      blinky.cli.Cli.main(Array("../.blinky.conf", "--version"))

      ps.flush()
      Console.setOut(systemOut)

      baos.toString mustEqual version
    }
  }
}
