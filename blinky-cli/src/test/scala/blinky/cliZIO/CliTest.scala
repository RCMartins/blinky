package blinky.cliZIO

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

import ammonite.ops.Path
import better.files.File
import blinky.BuildInfo.version
import blinky.TestSpec
import blinky.runZIO.Instruction
import blinky.runZIO.Instruction._
import blinky.runZIO.config.{MutationsConfigValidated, OptionsConfig, SimpleBlinkyConfig}
import blinky.runZIO.modules.Modules.{TestCliModule, TestParserModule}
import blinky.runZIO.modules.{CliModule, ParserModule}
import blinky.v0.Mutators
import scopt.DefaultOParserSetup
import zio.test.Assertion._
import zio.test._
import zio.test.environment._
import zio.{ExitCode, UIO}

import scala.concurrent.duration._

object CliTest extends TestSpec {

  private type InstructionType = Instruction[Either[ExitCode, MutationsConfigValidated], Path]

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Cli")(
      testM("blinky --version should return the version number of blinky") {
        val (zioInst, parser) = parse("--version")()
        for {
          _ <- zioInst
        } yield assert(parser.getOut) {
          equalTo(s"""blinky v$version
                     |""".stripMargin)
        } &&
          assert(parser.getErr)(equalTo(""))
      },
      testM("blinky --help should return the help text") {
        val (zioInst, parser) = parse("--help")()

        for {
          _ <- zioInst
        } yield assert(parser.getOut) {
          equalTo {
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
          }
        } &&
          assert(parser.getErr)(equalTo(""))
      },
      testM("blinky empty.conf should return the default config options") {
        val (zioInst, parser) = parse(getFilePath("empty.conf"))()

        for {
          inst <- zioInst
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(inst)(equalTo {
            Result(
              Right(
                MutationsConfigValidated(
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
              )
            )
          })
      },
      testM("blinky options1.conf should return the correct options") {
        val (zioInst, parser) = parse(getFilePath("options1.conf"))()

        for {
          inst <- zioInst
          config = getMutationsConfig(inst)
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(config.map(_.options))(equalTo {
            Some(
              OptionsConfig(
                verbose = false,
                dryRun = false,
                compileCommand = "",
                testCommand = "",
                maxRunningTime = 10.minutes,
                failOnMinimum = true,
                mutationMinimum = 66.7,
                onlyMutateDiff = false
              )
            )
          })
      },
      testM(
        "blinky simple1.conf should return the correct projectName, compileCommand and testCommand"
      ) {
        val (zioInst, parser) = parse(getFilePath("simple1.conf"))(File("."))

        for {
          inst <- zioInst
          config = getMutationsConfig(inst)
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(config.map(_.projectPath))(equalSome(File(".") / "examples" / "example1")) &&
          assert(config.map(_.filesToMutate))(equalSome("src/main/scala/Example.scala")) &&
          assert(config.map(_.options.compileCommand))(equalSome("example1")) &&
          assert(config.map(_.options.testCommand))(equalSome("example1"))
      },
      testM("blinky <no conf file> should return an error if there is no .blinky.conf file") {
        val pwdFolder = File(".")
        val (zioInst, parser) = parse()(pwdFolder)

        for {
          inst <- zioInst
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(inst)(equalTo {
            PrintErrorLine(
              s"""Default '${pwdFolder / ".blinky.conf"}' does not exist.
                 |blinky --help for usage.""".stripMargin,
              Result(Left(ExitCode(1)))
            )
          })
      },
      testM("blinky <non-existent-file> should return an error if there is no unknown.conf file") {
        val pwdFolder = File(getFilePath("."))
        val (zioInst, parser) = parse("unknown.conf")(pwdFolder)

        for {
          inst <- zioInst
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(inst)(equalTo {
            PrintErrorLine(
              s"""<blinkyConfFile> '${pwdFolder / "unknown.conf"}' does not exist.
                 |blinky --help for usage.""".stripMargin,
              Result(Left(ExitCode(1)))
            )
          })
      },
      suite("using overrides parameters")(
        testM("return the changed parameters") {
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

          val (zioInst, parser) = parse(getFilePath("empty.conf") +: params: _*)(File("."))

          for {
            inst <- zioInst
            config = getMutationsConfig(inst)
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(config.map(_.projectPath))(equalSome(File(".") / "examples" / "example2")) &&
            assert(config.map(_.filesToMutate))(equalSome("src/main/scala/Example.scala")) &&
            assert(config.map(_.filesToExclude))(equalSome("src/main/scala/Utils.scala")) &&
            assert(config.map(_.options))(equalSome {
              OptionsConfig(
                verbose = true,
                dryRun = true,
                compileCommand = "example2",
                testCommand = "example2",
                maxRunningTime = Duration(45, TimeUnit.MINUTES),
                failOnMinimum = true,
                mutationMinimum = 73.9,
                onlyMutateDiff = true
              )
            })
        },
        testM("return an error if projectPath does not exist") {
          val params: Seq[String] = Seq(
            "--projectPath",
            "examples/non-existent"
          )

          val pwd = File(".")
          val (zioInst, parser) = parse(getFilePath("empty.conf") +: params: _*)(pwd)

          for {
            inst <- zioInst
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(inst)(equalTo {
              PrintErrorLine(
                s"""--projectPath '${pwd / "examples" / "non-existent"}' does not exists.""".stripMargin,
                Result(Left(ExitCode(1)))
              )
            })
        }
      ),
      suite("mutationMinimum value check")(
        testM("return an error if mutationMinimum is negative") {
          val (zioInst, parser) = parse("--mutationMinimum", "-0.1")()

          for {
            inst <- zioInst
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(inst)(equalTo {
              PrintErrorLine(
                s"""mutationMinimum value is invalid. It should be a number between 0 and 100.""".stripMargin,
                Result(Left(ExitCode(1)))
              )
            })
        },
        testM("return an error if mutationMinimum is above 100") {
          val (zioInst, parser) = parse("--mutationMinimum", "100.1")()

          for {
            inst <- zioInst
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(inst)(equalTo {
              PrintErrorLine(
                s"""mutationMinimum value is invalid. It should be a number between 0 and 100.""".stripMargin,
                Result(Left(ExitCode(1)))
              )
            })
        }
      )
    )

  private def getMutationsConfig(instruction: InstructionType): Option[MutationsConfigValidated] =
    instruction match {
      case Result(value) =>
        value.toOption
      case _ =>
        None
    }

  class MyParser() {

    private val outCapture = new ByteArrayOutputStream
    private val errCapture = new ByteArrayOutputStream

    val _parser: DefaultOParserSetup =
      new DefaultOParserSetup() {

        override def terminate(exitState: Either[String, Unit]): Unit = ()

        override def displayToOut(msg: String): Unit =
          outCapture.write((msg + "\n").getBytes)

        override def displayToErr(msg: String): Unit =
          errCapture.write((msg + "\n").getBytes)

        override def errorOnUnknownArgument: Boolean = true

        override def reportError(msg: String): Unit =
          errCapture.write((msg + "\n").getBytes)

        override def reportWarning(msg: String): Unit =
          errCapture.write((msg + "\n").getBytes)

      }

    def getOut: String = removeCarriageReturns(outCapture.toString)
    def getErr: String = removeCarriageReturns(errCapture.toString)
  }

  def parse(
      args: String*
  )(
      pwd: File = File(getFilePath("."))
  ): (UIO[Instruction[Either[ExitCode, MutationsConfigValidated], Path]], MyParser) = {
    val myParser = new MyParser()

    val parserEnv: ParserModule with CliModule = new ParserModule with CliModule {
      override val parserModule: ParserModule.Service[Any] = new TestParserModule(myParser._parser)
      override val cliModule: CliModule.Service[Any] = new TestCliModule(pwd)
    }

    val zio = CliZIO.parse(args.toList).provide(parserEnv)
    (
      zio,
      myParser
    )
  }

}
