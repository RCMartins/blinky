package blinky.cli

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

import better.files.File
import blinky.BuildInfo
import blinky.BuildInfo.version
import blinky.run.{MutationsConfig, MutationsConfigValidated, OptionsConfig, SimpleBlinkyConfig}
import blinky.v0.{BlinkyConfig, Mutators}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{AppendedClues, OptionValues}
import scopt.DefaultOParserSetup

import scala.concurrent.duration._

class CliTest extends TestSpec {

  "blinky --version" should {

    "return the version number of blinky" in {
      val (_, out, err) = parse("--version")()

      out mustEqual
        s"""blinky v$version
           |""".stripMargin
      err mustBe empty
    }

  }

  "blinky --help" should {

    "return the help text" in {
      val (_, out, err) = parse("--help")()

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
           |  --compileCommand <cmd>   The compile command to be executed by sbt/bloop before the first run
           |  --testCommand <cmd>      The test command to be executed by sbt/bloop
           |  --verbose <bool>         If set, prints out debug information. Defaults to false
           |  --onlyMutateDiff <bool>  If set, only mutate added and edited files in git diff against the master branch
           |  --dryRun <bool>          If set, apply mutations and compile the code but do not run the actual mutation testing
           |  --maxRunningTime <duration>
           |                           Maximum time allowed to run mutation tests
           |  --mutationMinimum <decimal>
           |                           Minimum mutation score, value must be between 0 and 100, with one decimal place
           |  --failOnMinimum <bool>   If set, exits with non-zero code when the mutation score is below mutationMinimum value
           |""".stripMargin
      err mustBe empty
    }

  }

  "blinky empty.conf" should {

    "return the default config options" in {
      val (mutationsConfig, out, err) = parse(getFilePath("empty.conf"))()

      mutationsConfig.value mustEqual MutationsConfigValidated(
        projectPath = File(getFilePath(".")),
        filesToMutate = "src/main/scala",
        filesToExclude = "",
        mutators = SimpleBlinkyConfig(
          enabled = Mutators.all,
          disabled = Mutators(Nil)
        ),
        options = OptionsConfig(
          verbose = false,
          dryRun = false,
          compileCommand = "",
          testCommand = "",
          maxRunningTime = 60.minutes,
          failOnMinimum = false,
          mutationMinimum = 25.0,
          onlyMutateDiff = false
        )
      )

      out mustBe empty
      err mustBe empty
    }

  }

  "blinky options1.conf" should {

    "return the correct options" in {
      val (mutationsConfig, out, err) = parse(getFilePath("options1.conf"))()

      mutationsConfig.value.options mustEqual OptionsConfig(
        verbose = false,
        dryRun = false,
        compileCommand = "",
        testCommand = "",
        maxRunningTime = 10.minutes,
        failOnMinimum = true,
        mutationMinimum = 66.7,
        onlyMutateDiff = false
      )

      out mustBe empty
      err mustBe empty
    }

  }

  "blinky simple1.conf" should {

    "return the correct projectName, compileCommand and testCommand" in {
      val (mutationsConfigOpt, out, err) = parse(getFilePath("simple1.conf"))(File("."))
      val mutationsConfig = mutationsConfigOpt.value withClue err

      mutationsConfig.projectPath mustEqual File(".") / "examples" / "example1"
      mutationsConfig.filesToMutate mustEqual "src/main/scala/Example.scala"
      mutationsConfig.options.compileCommand mustEqual "example1"
      mutationsConfig.options.testCommand mustEqual "example1"

      out mustBe empty
      err mustBe empty
    }

  }

  "blinky <no conf file>" should {

    "return an error if there is no .blinky.conf file" in {
      val pwdFolder = File(".")
      val (mutationsConfigOpt, out, err) = parse()(pwdFolder)

      mutationsConfigOpt mustBe empty
      out mustBe empty
      err mustEqual
        s"""Default '${pwdFolder / ".blinky.conf"}' does not exist.
           |blinky --help for usage.
           |""".stripMargin
    }

  }

  "blinky <non-existent-file>" should {

    "return an error if there is no .blinky.conf file" in {
      val pwdFolder = File(getFilePath("."))
      val (mutationsConfigOpt, out, err) = parse("unknown.conf")(pwdFolder)

      mutationsConfigOpt mustBe empty
      out mustBe empty
      err mustEqual
        s"""<blinkyConfFile> '${pwdFolder / "unknown.conf"}' does not exist.
           |blinky --help for usage.
           |""".stripMargin
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
        "--verbose",
        "true",
        "--onlyMutateDiff",
        "true",
        "--dryRun",
        "true",
        "--maxRunningTime",
        "45 minutes",
        "--mutationMinimum",
        "73.9",
        "--failOnMinimum",
        "true"
      )

      val (mutationsConfigOpt, out, err) =
        parse(getFilePath("empty.conf") +: params: _*)(File("."))
      mutationsConfigOpt mustBe defined withClue err
      val mutationsConfig = mutationsConfigOpt.value

      mutationsConfig.projectPath mustEqual File(".") / "examples" / "example2"
      mutationsConfig.filesToMutate mustEqual "src/main/scala/Example.scala"
      mutationsConfig.filesToExclude mustEqual "src/main/scala/Utils.scala"

      mutationsConfig.options.compileCommand mustEqual "example2"
      mutationsConfig.options.testCommand mustEqual "example2"
      mutationsConfig.options.verbose mustEqual true
      mutationsConfig.options.onlyMutateDiff mustEqual true
      mutationsConfig.options.dryRun mustEqual true
      mutationsConfig.options.maxRunningTime mustEqual Duration(45, TimeUnit.MINUTES)
      mutationsConfig.options.mutationMinimum mustEqual 73.9
      mutationsConfig.options.failOnMinimum mustEqual true

      out mustBe empty
      err mustBe empty
    }

  }

  "mutationMinimum value check" should {

    "return an error if mutationMinimum is negative" in {
      val (mutationsConfigOpt, out, err) = parse("--mutationMinimum", "-0.1")()

      mutationsConfigOpt mustBe empty
      out mustBe empty
      err mustEqual
        """mutationMinimum value is invalid. It should be a number between 0 and 100.
          |""".stripMargin
    }

    "return an error if mutationMinimum is above 100" in {
      val (mutationsConfigOpt, out, err) = parse("--mutationMinimum", "100.1")()

      mutationsConfigOpt mustBe empty
      out mustBe empty
      err mustEqual
        """mutationMinimum value is invalid. It should be a number between 0 and 100.
          |""".stripMargin
    }

  }

  private val _parser =
    new DefaultOParserSetup() {
      override def terminate(exitState: Either[String, Unit]): Unit = ()
    }

  private def parse(
      args: String*
  )(
      pwd: File = File(getFilePath("."))
  ): (Option[MutationsConfigValidated], String, String) = {
    val outCapture = new ByteArrayOutputStream
    val errCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      Console.withErr(errCapture) {
        val mutationsConfig =
          Cli.parse(args.toArray, _parser)(pwd)
        (
          mutationsConfig,
          removeCarriageReturns(outCapture.toString),
          removeCarriageReturns(errCapture.toString)
        )
      }
    }
  }

  private final val inWindows: Boolean =
    System.getProperty("os.name").toLowerCase.contains("win")

  private final val removeCarriageReturns: String => String =
    if (inWindows) _.replace("\r", "") else identity

  private def getFilePath(fileName: String): String =
    if (inWindows)
      getClass.getResource(s"/$fileName").getPath.stripPrefix("/")
    else
      getClass.getResource(s"/$fileName").getPath

}
