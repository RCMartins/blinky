package blinky.cli

import blinky.BuildInfo
import blinky.BuildInfo.version
import blinky.run.{MutationsConfig, OptionsConfig}
import blinky.v0.{BlinkyConfig, Mutators}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.FixtureAnyWordSpec
import org.scalatest.{OptionValues, Outcome}
import scopt.{OParserSetup, RenderingMode}

import scala.concurrent.duration._

class CliTest extends FixtureAnyWordSpec with Matchers with OptionValues {

  override def withFixture(test: OneArgTest): Outcome = {
    test(new FixtureParam)
  }

  "Cli general parsing" when {

    "--version" should {

      "return the version number of blinky" in { test =>
        Cli.parse(Array("--version"), test.oParser)

        test.outLines mustEqual Seq(s"blinky v$version")
      }

    }

    "--help" should {

      "return the version number of blinky" in { test =>
        Cli.parse(Array("--help"), test.oParser)

        test.outLines.mkString mustEqual
          s"""blinky v$version
             |Usage: blinky [options] [<blinkyConfFile>]
             |
             |  --help                   prints this usage text
             |  -v, --version            prints blinky version
             |  <blinkyConfFile>
             |  --projectName <path>     The project name, used for bloop compile and test commands
             |  --projectPath <path>     The project directory, can be an absolute or relative path
             |  --filesToMutate <path>   The relative path to the scala src folder or files to mutate
             |  --blinkyVersion <version>
             |                           The Blinky version to be used to mutate the code
             |  --compileCommand <command>
             |                           The compile command to be executed by sbt/bloop before the first run
             |  --testCommand <command>  The test command to be executed by sbt/bloop
             |  --verbose <bool>         If set, prints out debug information. Defaults to false""".stripMargin
      }

    }

    "empty.conf" should {

      "return the default config options" in { test =>
        val mutationsConfig = Cli.parse(Array(getFilePath("empty.conf")), test.oParser).value

        mutationsConfig mustEqual MutationsConfig(
          projectPath = ".",
          projectName = "",
          filesToMutate = "src/main/scala",
          conf = BlinkyConfig(
            projectPath = "",
            mutatorsPath = "",
            enabledMutators = Mutators.all,
            disabledMutators = Mutators(Nil)
          ),
          blinkyVersion = BuildInfo.version,
          options = OptionsConfig(
            verbose = false,
            dryRun = false,
            compileCommand = "",
            testCommand = "",
            maxRunningTime = 60.minutes,
            failOnMinimum = false,
            mutationMinimum = 25.0
          )
        )
      }

    }

    "options1.conf" should {

      "return the correct options" in { test =>
        val mutationsConfig = Cli.parse(Array(getFilePath("options1.conf")), test.oParser).value

        mutationsConfig.options mustEqual OptionsConfig(
          verbose = false,
          dryRun = false,
          compileCommand = "",
          testCommand = "",
          maxRunningTime = 10.minutes,
          failOnMinimum = true,
          mutationMinimum = 66.7
        )
      }

    }

    "simple1.conf" should {

      "return the correct projectName, compileCommand and testCommand" in { test =>
        val mutationsConfig = Cli.parse(Array(getFilePath("simple1.conf")), test.oParser).value

        mutationsConfig.projectPath mustEqual "examples/example1"
        mutationsConfig.projectName mustEqual "example1"
        mutationsConfig.filesToMutate mustEqual "src/main/scala/Example.scala"
        mutationsConfig.options.compileCommand mustEqual "example1"
        mutationsConfig.options.testCommand mustEqual "example1"
      }

    }

    "using overrides parameters" should {

      "return the changed parameters" in { test =>
        val params: Array[String] = Array(
          "--projectPath",
          "examples/example2",
          "--projectName",
          "example2",
          "--filesToMutate",
          "src/main/scala/Example.scala",
          "--blinkyVersion",
          "0.2.0",
          "--verbose",
          "true"
        )

        val mutationsConfig =
          Cli.parse(Array[String](getFilePath("empty.conf")) ++ params, test.oParser).value

        mutationsConfig.projectPath mustEqual "examples/example2"
        mutationsConfig.projectName mustEqual ""
        mutationsConfig.filesToMutate mustEqual "src/main/scala/Example.scala"
        mutationsConfig.blinkyVersion mustEqual "0.2.0"

        mutationsConfig.options.compileCommand mustEqual "example2"
        mutationsConfig.options.testCommand mustEqual "example2"
        mutationsConfig.options.verbose mustEqual true
      }

    }

  }

  class FixtureParam {

    var outLines: Seq[String] = Seq.empty

    val oParser: OParserSetup = new OParserSetup {
      override def renderingMode: RenderingMode = RenderingMode.TwoColumns

      override def errorOnUnknownArgument: Boolean = true

      override def showUsageOnError: Option[Boolean] = Some(false)

      override def displayToOut(msg: String): Unit = {
        outLines = outLines :+ msg
      }

      override def displayToErr(msg: String): Unit =
        fail(s"displayToErr: $msg")

      override def reportError(msg: String): Unit =
        fail(s"reportError: $msg")

      override def reportWarning(msg: String): Unit =
        fail(s"reportWarning: $msg")

      override def terminate(exitState: Either[String, Unit]): Unit = ()
    }

  }

  private def inWindows: Boolean =
    System.getProperty("os.name").toLowerCase.contains("win")

  private def getFilePath(fileName: String): String =
    if (inWindows)
      getClass.getResource(s"/$fileName").getPath.stripPrefix("/")
    else
      getClass.getResource(s"/$fileName").getPath

}
