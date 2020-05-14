package blinky.cli

import blinky.BuildInfo.version
import org.scalatest.{MustMatchers, Outcome, fixture}
import scopt.{OParserSetup, RenderingMode}

class CliTest extends fixture.WordSpec with MustMatchers {

  override def withFixture(test: OneArgTest): Outcome = {
    test(new FixtureParam)
  }

  "Cli general parsing" when {

    "-version" should {

      "return the version number of blinky" in { test =>
        Cli.parse(Array(getFilePath("empty.blinky.conf"), "--version"), test.oParser)

        test.outLines mustEqual Seq(s"blinky v$version")
      }

    }

  }

  class FixtureParam {

    var outLines: Seq[String] = Seq.empty

    val oParser: OParserSetup = new OParserSetup {
      override def renderingMode: RenderingMode = RenderingMode.OneColumn

      override def errorOnUnknownArgument: Boolean = true

      override def showUsageOnError: Option[Boolean] = Some(false)

      override def displayToOut(msg: String): Unit = {
        outLines = outLines :+ msg
      }

      override def displayToErr(msg: String): Unit = { println(s"displayToErr: $msg"); ??? }

      override def reportError(msg: String): Unit = { println(s"reportError: $msg"); ??? }

      override def reportWarning(msg: String): Unit = { println(s"reportWarning: $msg"); ??? }

      override def terminate(exitState: Either[String, Unit]): Unit = ()
    }

  }

  private def getFilePath(fileName: String): String =
    getClass.getResource(s"/$fileName").getPath.stripPrefix("/")

}
