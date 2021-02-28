package blinky.cli

import better.files.File
import blinky.BuildInfo.version
import blinky.TestSpec
import blinky.run.config.{MutationsConfigValidated, OptionsConfig, SimpleBlinkyConfig}
import blinky.run.modules.{CliModule, ParserModule, TestModules}
import blinky.v0.Mutators
import scopt.DefaultOParserSetup
import zio.test.Assertion._
import zio.test._
import zio.test.environment._
import zio.{Layer, UIO}

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

object CliTest extends TestSpec {

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Cli")(
      testM("blinky --version should return the version number of blinky") {
        val (zioResult, parser) = parse("--version")()
        for {
          _ <- zioResult
        } yield assert(parser.getOut)(equalTo {
          s"""blinky v$version
             |""".stripMargin
        }) &&
          assert(parser.getErr)(equalTo(""))
      },
      testM("blinky --help should return the help text") {
        val (zioResult, parser) = parse("--help")()

        for {
          _ <- zioResult
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
               |  --multiRun <job-index/number-of-jobs>
               |                           Only test the mutants of the given index, 1 <= job-index <= number-of-jobs
               |  --timeoutFactor <decimal>
               |                           Time factor for each mutant test (default: 1.5)
               |  --timeout <duration>     Duration of additional flat timeout for each mutant test (default: 1 second)
               |""".stripMargin
          }
        } &&
          assert(parser.getErr)(equalTo(""))
      },
      testM("blinky empty.conf should return the default config options") {
        val (zioResult, parser) = parse(getFilePath("empty.conf"))()

        for {
          result <- zioResult
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(result)(equalTo {
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
                  onlyMutateDiff = false,
                  multiRun = (1, 1),
                  timeoutFactor = 1.5,
                  timeout = 1.second
                )
              )
            )
          })
      },
      testM("blinky options1.conf should return the correct options") {
        val (zioResult, parser) = parse(getFilePath("options1.conf"))()

        for {
          result <- zioResult
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(result.map(_.options))(equalTo {
            Right(
              OptionsConfig(
                verbose = false,
                dryRun = false,
                compileCommand = "",
                testCommand = "",
                maxRunningTime = 10.minutes,
                failOnMinimum = true,
                mutationMinimum = 66.7,
                onlyMutateDiff = false,
                multiRun = (1, 3),
                timeoutFactor = 2.0,
                timeout = 5.second
              )
            )
          })
      },
      testM(
        "blinky simple1.conf should return the correct projectName, compileCommand and testCommand"
      ) {
        val (zioResult, parser) = parse(getFilePath("simple1.conf"))(File("."))

        for {
          result <- zioResult
          config = result.toOption
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(config.map(_.projectPath))(
            equalSome(File(".") / "ci-tests" / "examples" / "example1")
          ) &&
          assert(config.map(_.filesToMutate))(equalSome("src/main/scala/Example.scala")) &&
          assert(config.map(_.options.compileCommand))(equalSome("example1")) &&
          assert(config.map(_.options.testCommand))(equalSome("example1"))
      },
      testM("blinky <no conf file> should return an error if there is no .blinky.conf file") {
        val pwdFolder = File(".")
        val (zioResult, parser) = parse()(pwdFolder)

        for {
          result <- zioResult
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(result)(equalTo {
            Left(
              s"""Default '${pwdFolder / ".blinky.conf"}' does not exist.
                 |blinky --help for usage.""".stripMargin
            )
          })
      },
      testM("blinky <non-existent-file> should return an error if there is no unknown.conf file") {
        val pwdFolder = File(getFilePath("."))
        val (zioResult, parser) = parse("unknown.conf")(pwdFolder)

        for {
          result <- zioResult
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(result)(equalTo {
            Left(
              s"""<blinkyConfFile> '${pwdFolder / "unknown.conf"}' does not exist.
                 |blinky --help for usage.""".stripMargin
            )
          })
      },
      testM("return an error multiRun field in wrong") {
        val (zioResult, parser) = parse(getFilePath("wrongMultiRun.conf"))()

        for {
          result <- zioResult
        } yield assert(parser.getOut)(equalTo("")) &&
          assert(parser.getErr)(equalTo("")) &&
          assert(result)(equalTo(Left("Invalid value, should be in 'int/int' format")))
      },
      suite("using overrides parameters")(
        testM("return the changed parameters") {
          val params: Seq[String] = Seq(
            "--projectPath",
            "ci-tests/examples/example2",
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
            "true",
            "--multiRun",
            "2/3",
            "--timeoutFactor",
            "1.75",
            "--timeout",
            "3 seconds"
          )

          val (zioResult, parser) = parse(getFilePath("empty.conf") +: params: _*)(File("."))

          for {
            result <- zioResult
            config = result.toOption
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(config.map(_.projectPath))(
              equalSome(File(".") / "ci-tests" / "examples" / "example2")
            ) &&
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
                onlyMutateDiff = true,
                multiRun = (2, 3),
                timeoutFactor = 1.75,
                timeout = 3.second
              )
            })
        },
        testM("return an error if projectPath does not exist") {
          val params: Seq[String] = Seq(
            "--projectPath",
            "examples/non-existent"
          )

          val pwd = File(".")
          val (zioResult, parser) = parse(getFilePath("empty.conf") +: params: _*)(pwd)

          for {
            result <- zioResult
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(result)(equalTo {
              Left(
                s"""--projectPath '${pwd / "examples" / "non-existent"}' does not exists.""".stripMargin
              )
            })
        }
      ),
      suite("mutationMinimum value check")(
        testM("return an error if mutationMinimum is negative") {
          val (zioResult, parser) = parse("--mutationMinimum", "-0.1")()

          for {
            result <- zioResult
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(result)(equalTo {
              Left(
                s"""mutationMinimum value is invalid. It should be a number between 0 and 100.""".stripMargin
              )
            })
        },
        testM("return an error if mutationMinimum is above 100") {
          val (zioResult, parser) = parse("--mutationMinimum", "100.1")()

          for {
            result <- zioResult
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(equalTo("")) &&
            assert(result)(equalTo {
              Left(
                s"""mutationMinimum value is invalid. It should be a number between 0 and 100.""".stripMargin
              )
            })
        },
        testM("return an error multiRun field in wrong (less than 1)") {
          val (zioResult, parser) = parse("--multiRun", "0/1")()

          for {
            _ <- zioResult
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(
              equalTo(
                """Error: Option --multiRun failed when given '0/1'. Invalid values, they should be >= 1
                  |Try --help for more information.
                  |""".stripMargin
              )
            )
        },
        testM("return an error multiRun field in wrong (index <= total)") {
          val (zioResult, parser) = parse("--multiRun", "3/2")()

          for {
            _ <- zioResult
          } yield assert(parser.getOut)(equalTo("")) &&
            assert(parser.getErr)(
              equalTo(
                """Error: Option --multiRun failed when given '3/2'. Invalid values, they should be >= 1
                  |Try --help for more information.
                  |""".stripMargin
              )
            )
        }
      )
    )

  private class MyParser() {
    private val outCapture = new ByteArrayOutputStream
    private val errCapture = new ByteArrayOutputStream

    val _parser: DefaultOParserSetup =
      new DefaultOParserSetup() {
        override def terminate(exitState: Either[String, Unit]): Unit = ()
        override def displayToOut(msg: String): Unit = outCapture.write((msg + "\n").getBytes)
        override def displayToErr(msg: String): Unit = errCapture.write((msg + "\n").getBytes)
        override def errorOnUnknownArgument: Boolean = true
      }

    def getOut: String = removeCarriageReturns(outCapture.toString)
    def getErr: String = removeCarriageReturns(errCapture.toString)
  }

  private def parse(
      args: String*
  )(
      pwd: File = File(getFilePath("."))
  ): (UIO[Either[String, MutationsConfigValidated]], MyParser) = {
    val myParser = new MyParser()

    val parserEnv: Layer[Nothing, ParserModule with CliModule] =
      TestModules.testParserModule(myParser._parser) ++
        TestModules.testCliModule(pwd)

    (Cli.parse(args.toList).provideLayer(parserEnv), myParser)
  }

}
