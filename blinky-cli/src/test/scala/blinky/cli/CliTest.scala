package blinky.cli

import java.io.ByteArrayOutputStream

import blinky.BuildInfo
import blinky.BuildInfo.version
import blinky.run.{MutationsConfig, OptionsConfig}
import blinky.v0.{BlinkyConfig, Mutators}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{AppendedClues, OptionValues}
import scopt.DefaultOParserSetup

import scala.concurrent.duration._

class CliTest extends AnyWordSpec with Matchers with OptionValues with AppendedClues {

  "Cli general parsing" when {

    "--version" should {

      "return the version number of blinky" in {
        val (_, out, _) = parse("--version")

        out mustEqual
          s"""blinky v$version
             |""".stripMargin
      }

    }

    "--help" should {

      "return the version number of blinky" in {
        val (_, out, _) = parse("--help")

        out mustEqual
          s"""blinky v$version
             |Usage: blinky [options] [<blinkyConfFile>]
             |
             |  --help                   prints this usage text
             |  -v, --version            prints blinky version
             |  <blinkyConfFile>
             |  --projectName <path>     The project name, used for bloop compile and test commands
             |  --projectPath <path>     The project directory, can be an absolute or relative path
             |  --filesToMutate <path>   The relative path to the scala src folder or files to mutate
             |  --filesToExclude <path>  The relative path to the folder or files to exclude from mutation
             |  --blinkyVersion <ver>    The Blinky version to be used to mutate the code
             |  --compileCommand <cmd>   The compile command to be executed by sbt/bloop before the first run
             |  --testCommand <cmd>      The test command to be executed by sbt/bloop
             |  --verbose <bool>         If set, prints out debug information. Defaults to false
             |""".stripMargin
      }

    }

    "empty.conf" should {

      "return the default config options" in {
        val (mutationsConfig, _, _) = parse(getFilePath("empty.conf"))

        mutationsConfig.value mustEqual MutationsConfig(
          projectPath = ".",
          projectName = "",
          filesToMutate = "src/main/scala",
          filesToExclude = "",
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

      "return the correct options" in {
        val (mutationsConfig, _, _) = parse(getFilePath("options1.conf"))

        mutationsConfig.value.options mustEqual OptionsConfig(
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

      "return the correct projectName, compileCommand and testCommand" in {
        val (mutationsConfigOpt, _, _) = parse(getFilePath("simple1.conf"))
        val mutationsConfig = mutationsConfigOpt.value

        mutationsConfig.projectPath mustEqual "examples/example1"
        mutationsConfig.projectName mustEqual "example1"
        mutationsConfig.filesToMutate mustEqual "src/main/scala/Example.scala"
        mutationsConfig.options.compileCommand mustEqual "example1"
        mutationsConfig.options.testCommand mustEqual "example1"
      }

    }

    "using overrides parameters" should {

      "return the changed parameters" in {
        val params: Seq[String] = Seq(
          "--projectPath",
          "examples/example2",
          "--projectName",
          "example2",
          "--filesToMutate",
          "src/main/scala/Example.scala",
          "--filesToExclude",
          "src/main/scala/Utils.scala",
          "--blinkyVersion",
          "0.2.0",
          "--verbose",
          "true"
        )

        val (mutationsConfigOpt, _, err) = parse(getFilePath("empty.conf") +: params: _*)
        mutationsConfigOpt mustBe defined withClue err
        val mutationsConfig = mutationsConfigOpt.value

        mutationsConfig.projectPath mustEqual "examples/example2"
        mutationsConfig.projectName mustEqual ""
        mutationsConfig.filesToMutate mustEqual "src/main/scala/Example.scala"
        mutationsConfig.filesToExclude mustEqual "src/main/scala/Utils.scala"
        mutationsConfig.blinkyVersion mustEqual "0.2.0"

        mutationsConfig.options.compileCommand mustEqual "example2"
        mutationsConfig.options.testCommand mustEqual "example2"
        mutationsConfig.options.verbose mustEqual true
      }

    }

  }

  private val _parser =
    new DefaultOParserSetup() {
      override def terminate(exitState: Either[String, Unit]): Unit = ()
    }

  private def parse(args: String*): (Option[MutationsConfig], String, String) = {
    val outCapture = new ByteArrayOutputStream
    val errCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      Console.withErr(errCapture) {
        val mutationsConfig = Cli.parse(args.toArray, _parser)
        (
          mutationsConfig,
          outCapture.toString,
          errCapture.toString
        )
      }
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
