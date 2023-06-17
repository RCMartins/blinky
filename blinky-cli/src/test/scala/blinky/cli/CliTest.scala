package blinky.cli

import better.files.File
import blinky.BuildInfo.version
import blinky.TestSpec._
import blinky.run.config.FileFilter.{FileName, SingleFileOrFolder}
import blinky.run.config._
import blinky.run.modules.{CliModule, ParserModule, TestModules}
import blinky.v0.{MutantRange, Mutators}
import os.RelPath
import scopt.DefaultOEffectSetup
import zio.test._
import zio.{Layer, UIO}

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

object CliTest extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("Cli")(
      test("blinky --version should return the version number of blinky") {
        val (zioResult, parser) = parse("--version")()
        for {
          _ <- zioResult
        } yield assertTrue(
          parser.getOut ==
            s"""blinky v$version
               |""".stripMargin,
          parser.getErr == ""
        )
      },
      test("blinky --help should return the help text") {
        val (zioResult, parser) = parse("--help")()

        for {
          _ <- zioResult
        } yield assertTrue(
          parser.getOut ==
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
               |  --copyGitFolder <bool>   If set, also copies the .git folder to the temporary project directory (default false)
               |  --testRunner <runner>    The test runner to be used by blinky, "sbt" or "bloop" (default "bloop")
               |  --compileCommand <cmd>   The compile command to be executed by sbt/bloop before the first run
               |  --testCommand <cmd>      The test command to be executed by sbt/bloop
               |  --verbose <bool>         If set, prints out debug information. Defaults to false
               |  --onlyMutateDiff <bool>  If set, only mutate added and edited files in git diff against the main branch
               |  --mainBranch <branch>    Sets the main branch to compare against when using --onlyMutateDiff (default 'main')
               |  --dryRun <bool>          If set, apply mutations and compile the code but do not run the actual mutation testing
               |  --maxRunningTime <duration>
               |                           Maximum time allowed to run mutation tests
               |  --mutationMinimum <decimal>
               |                           Minimum mutation score, value must be between 0 and 100, with one decimal place
               |  --failOnMinimum <bool>   If set, exits with non-zero code when the mutation score is below mutationMinimum value
               |  --multiRun <job-index/amount-of-jobs>
               |                           Only test the mutants of the given index, 1 <= job-index <= amount-of-jobs
               |  --timeoutFactor <decimal>
               |                           Time factor for each mutant test
               |  --timeout <duration>     Duration of additional flat timeout for each mutant test
               |  --testInOrder <bool>     If set, forces the mutants to be tested in order: 1,2,3,... (default false)
               |  --mutant <range>         Mutant indices to test. Defaults to 1-2147483647
               |""".stripMargin,
          parser.getErr == ""
        )
      },
      test("blinky empty.conf should return the default config options") {
        val (zioResult, parser) =
          parse(getFilePath("empty.conf"))(File(getFilePath("some-project")))

        for {
          result <- zioResult
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          result == Right(
            MutationsConfigValidated(
              projectPath = File(getFilePath("some-project")),
              filesToMutate = SingleFileOrFolder(RelPath("src/main/scala")),
              filesToExclude = "",
              mutators = SimpleBlinkyConfig(
                enabled = Mutators.all,
                disabled = Mutators(Nil)
              ),
              options = OptionsConfig(
                copyGitFolder = false,
                verbose = false,
                dryRun = false,
                testRunner = TestRunnerType.Bloop,
                compileCommand = "",
                testCommand = "",
                maxRunningTime = 60.minutes,
                failOnMinimum = false,
                mutationMinimum = 25.0,
                onlyMutateDiff = false,
                mainBranch = "main",
                mutant = Seq(MutantRange(1, Int.MaxValue)),
                multiRun = (1, 1),
                timeoutFactor = 1.5,
                timeout = 5.second,
                testInOrder = false,
              )
            )
          )
        )
      },
      test("blinky options1.conf should return the correct options") {
        val (zioResult, parser) =
          parse(getFilePath("options1.conf"))(File(getFilePath("some-project")))

        for {
          result <- zioResult
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          result.map(_.options) == Right(
            OptionsConfig(
              copyGitFolder = true,
              verbose = false,
              dryRun = false,
              testRunner = TestRunnerType.SBT,
              compileCommand = "",
              testCommand = "",
              maxRunningTime = 10.minutes,
              failOnMinimum = true,
              mutationMinimum = 66.7,
              onlyMutateDiff = false,
              mainBranch = "master",
              mutant = Seq(MutantRange(5, 20)),
              multiRun = (1, 3),
              timeoutFactor = 2.0,
              timeout = 10.second,
              testInOrder = true,
            )
          )
        )
      },
      test("blinky simple1.conf returns the correct projectName, compileCommand and testCommand") {
        val (zioResult, parser) = parse(getFilePath("simple1.conf"))()
        for {
          result <- zioResult
        } yield {
          lazy val config = result.toOption.get
          assertTrue(
            parser.getOut == "",
            parser.getErr == "",
            config.projectPath == File(getFilePath("some-project")),
            config.filesToMutate == SingleFileOrFolder(RelPath("src/main/scala/Example.scala")),
            config.options.compileCommand == "example1",
            config.options.testCommand == "example1"
          )
        }
      },
      test("blinky simple2.conf returns the correct projectName, compileCommand and testCommand") {
        val (zioResult, parser) = parse(getFilePath("simple2.conf"))()
        for {
          result <- zioResult
        } yield {
          lazy val config = result.toOption.get
          assertTrue(
            parser.getOut == "",
            parser.getErr == "",
            result.isRight,
            config.projectPath == File(getFilePath("some-project")),
            config.filesToMutate == SingleFileOrFolder(RelPath("src/main/scala/Example.scala")),
            config.options.compileCommand == "example1",
            config.options.testCommand == "example1",
          )
        }
      },
      test("blinky wrongPath1.conf returns a fileName object") {
        val (zioResult, parser) = parse(getFilePath("wrongPath1.conf"))()
        for {
          result <- zioResult
          config = result.toOption
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          config.map(_.filesToMutate).contains(FileName("src/main/scala/UnknownFile.scala"))
        )
      },
      test("blinky wrongPath2.conf returns a fileName object") {
        val (zioResult, parser) = parse(getFilePath("wrongPath2.conf"))()
        for {
          result <- zioResult
          config = result.toOption
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          config.map(_.filesToMutate).contains(FileName("src/main/scala/UnknownFile.scala"))
        )
      },
      test("blinky <no conf file> returns an error if there is no default .blinky.conf file") {
        val pwdFolder = File(".")
        val (zioResult, parser) = parse()(pwdFolder)
        for {
          result <- zioResult
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          result == Left(
            s"""Default '${pwdFolder / ".blinky.conf"}' does not exist.
               |blinky --help for usage.""".stripMargin
          )
        )
      },
      test("blinky <non-existent-file> returns an error if there is no unknown.conf file") {
        val pwdFolder = File(getFilePath("."))
        val (zioResult, parser) = parse("unknown.conf")(pwdFolder)
        for {
          result <- zioResult
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          result == Left(
            s"""<blinkyConfFile> '${pwdFolder / "unknown.conf"}' does not exist.
               |blinky --help for usage.""".stripMargin
          )
        )
      },
      test("return an error if multiRun field in wrong") {
        val (zioResult, parser) = parse(getFilePath("wrongMultiRun.conf"))()
        for {
          result <- zioResult
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          result == Left(
            """<input>:2:0 error: Type mismatch;
              |  found    : String (value: "1-2")
              |  expected : Invalid value, should be a String in 'int/int' format
              |  multiRun = "1-2"
              |^
              |""".stripMargin
          )
        )
      },
      test("return an error if testRunner field in wrong") {
        val (zioResult, parser) = parse(getFilePath("wrongTestRunner.conf"))()
        for {
          result <- zioResult
        } yield assertTrue(
          parser.getOut == "",
          parser.getErr == "",
          result == Left("Invalid runner type. Should be 'sbt' or 'bloop'.")
        )
      },
      suite("using overrides parameters")(
        test("return the changed parameters") {
          val params: Seq[String] = Seq(
            "--projectPath",
            "some-project",
            "--projectName",
            "example2",
            "--filesToMutate",
            "src/main/scala/Main.scala",
            "--filesToExclude",
            "src/main/scala/Utils.scala",
            "--testRunner",
            "SBT",
            "--copyGitFolder",
            "true",
            "--verbose",
            "true",
            "--onlyMutateDiff",
            "true",
            "--mainBranch",
            "dev",
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
            "3 seconds",
            "--testInOrder",
            "true",
            "--mutant",
            "10-50",
          )

          val (zioResult, parser) = parse(getFilePath("empty.conf") +: params: _*)()
          for {
            result <- zioResult
          } yield {
            lazy val config = result.toOption.get
            assertTrue(
              parser.getOut == "",
              parser.getErr == "",
              result.isRight,
              config.projectPath == File(getFilePath("some-project")),
              config.filesToMutate == SingleFileOrFolder(RelPath("src/main/scala/Main.scala")),
              config.filesToExclude == "src/main/scala/Utils.scala",
              config.options == OptionsConfig(
                copyGitFolder = true,
                verbose = true,
                dryRun = true,
                testRunner = TestRunnerType.SBT,
                compileCommand = "example2",
                testCommand = "example2",
                maxRunningTime = Duration(45, TimeUnit.MINUTES),
                failOnMinimum = true,
                mutationMinimum = 73.9,
                onlyMutateDiff = true,
                mainBranch = "dev",
                mutant = Seq(MutantRange(10, 50)),
                multiRun = (2, 3),
                timeoutFactor = 1.75,
                timeout = 3.second,
                testInOrder = true,
              )
            )
          }
        },
        test("return an error if projectPath does not exist") {
          val params: Seq[String] = Seq(
            "--projectPath",
            "non-existent/project-path"
          )

          val pwd = File(getFilePath("."))
          val (zioResult, parser) = parse(getFilePath("empty.conf") +: params: _*)(pwd)
          for {
            result <- zioResult
          } yield assertTrue(
            parser.getOut == "",
            parser.getErr == "",
            result == Left(
              s"""--projectPath '${pwd / "non-existent" / "project-path"}' does not exist."""
            )
          )
        },
        test("return the correct testRunner for --testRunner=Bloop") {
          val params: Seq[String] = Seq(
            "--testRunner",
            "Bloop"
          )

          val pwd = File(getFilePath("."))
          val (zioResult, parser) = parse(getFilePath("empty.conf") +: params: _*)(pwd)
          for {
            result <- zioResult
            config = result.toOption
          } yield assertTrue(
            parser.getOut == "",
            parser.getErr == "",
            config.map(_.options.testRunner).contains(TestRunnerType.Bloop)
          )
        }
      ),
      suite("mutationMinimum value check")(
        test("return an error if mutationMinimum is negative") {
          val (zioResult, parser) = parse("--mutationMinimum", "-0.1")()
          for {
            result <- zioResult
          } yield assertTrue(
            parser.getOut == "",
            parser.getErr == "",
            result == Left(
              "mutationMinimum value is invalid. It should be a number between 0 and 100."
            )
          )
        },
        test("return an error if mutationMinimum is above 100") {
          val (zioResult, parser) = parse("--mutationMinimum", "100.1")()

          for {
            result <- zioResult
          } yield assertTrue(
            parser.getOut == "",
            parser.getErr == "",
            result == Left(
              "mutationMinimum value is invalid. It should be a number between 0 and 100."
            )
          )
        },
        test("return an error multiRun field in wrong (less than 1)") {
          val (zioResult, parser) = parse("--multiRun", "0/1")()
          for {
            _ <- zioResult
          } yield assertTrue(
            parser.getOut == "",
            parser.getErr ==
              """Error: Option --multiRun failed when given '0/1'. Invalid index value, should be >= 1
                |Try --help for more information.
                |""".stripMargin
          )
        },
        test("return an error multiRun field in wrong (index <= total)") {
          val (zioResult, parser) = parse("--multiRun", "3/2")()
          for {
            _ <- zioResult
          } yield assertTrue(
            parser.getOut == "",
            parser.getErr ==
              """Error: Option --multiRun failed when given '3/2'. Invalid amount, should be greater or equal than index
                |Try --help for more information.
                |""".stripMargin
          )
        }
      )
    )

  private class MyParser() {
    private val outCapture = new ByteArrayOutputStream
    private val errCapture = new ByteArrayOutputStream

    val _parser: DefaultOEffectSetup =
      new DefaultOEffectSetup {
        override def terminate(exitState: Either[String, Unit]): Unit = ()
        override def displayToOut(msg: String): Unit = outCapture.write((msg + "\n").getBytes)
        override def displayToErr(msg: String): Unit = errCapture.write((msg + "\n").getBytes)
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
