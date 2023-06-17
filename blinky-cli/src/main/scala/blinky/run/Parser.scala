package blinky.run

import blinky.BuildInfo
import blinky.run.config.{Args, OptionsConfig, TestRunnerType}
import blinky.v0.MutantRange
import com.softwaremill.quicklens._
import metaconfig.{Conf, Configured}
import scopt.{OParser, OParserBuilder, Read}

import scala.concurrent.duration.Duration

object Parser {

  private def readMultiRun: Read[(Int, Int)] =
    new Read[(Int, Int)] {
      override def arity: Int = 1

      override def reads: String => (Int, Int) =
        OptionsConfig.stringToMultiRunParser(_) match {
          case Left(message) => throw new IllegalArgumentException(message)
          case Right(value)  => value
        }
    }

  private def readTestRunnerType: Read[TestRunnerType] =
    new Read[TestRunnerType] {
      override def arity: Int = 1

      override def reads: String => TestRunnerType =
        str =>
          TestRunnerType.decoder.read(Conf.Str(str)).toEither match {
            case Left(confError) => throw new IllegalArgumentException(confError.msg)
            case Right(value)    => value
          }
    }

  private val builder: OParserBuilder[Args] = OParser.builder[Args]
  val parser: OParser[Unit, Args] = {
    import builder._
    OParser.sequence(
      programName("blinky"),
      head("blinky", s"v${BuildInfo.version}"),
      help("help")
        .text("prints this usage text"),
      version('v', "version")
        .text("prints blinky version"),
      arg[String]("<blinkyConfFile>")
        .action((confFile, args) => args.copy(mainConfFile = Some(confFile)))
        .optional()
        .maxOccurs(1),
      opt[String]("projectName")
        .valueName("<path>")
        .action { (projectName, config) =>
          config.add(
            _.modifyAll(_.options.compileCommand, _.options.testCommand).setTo(projectName)
          )
        }
        .text("The project name, used for bloop compile and test commands")
        .maxOccurs(1),
      opt[String]("projectPath")
        .valueName("<path>")
        .action((projectPath, config) => config.add(_.copy(projectPath = projectPath)))
        .text("The project directory, can be an absolute or relative path")
        .maxOccurs(1),
      opt[String]("filesToMutate")
        .valueName("<path>")
        .action((filesToMutate, config) => config.add(_.copy(filesToMutate = filesToMutate)))
        .text("The relative path to the scala src folder or files to mutate")
        .maxOccurs(1),
      opt[String]("filesToExclude")
        .valueName("<path>")
        .action((filesToExclude, config) => config.add(_.copy(filesToExclude = filesToExclude)))
        .text("The relative path to the folder or files to exclude from mutation")
        .maxOccurs(1),
      opt[Boolean]("copyGitFolder")
        .valueName("<bool>")
        .action { (copyGitFolder, config) =>
          config.add(_.modify(_.options.copyGitFolder).setTo(copyGitFolder))
        }
        .text(
          "If set, also copies the .git folder to the temporary project directory (default false)"
        )
        .maxOccurs(1),
      opt[TestRunnerType]("testRunner")(readTestRunnerType)
        .valueName("<runner>")
        .action { (testRunnerType, config) =>
          config.add(_.modify(_.options.testRunner).setTo(testRunnerType))
        }
        .text("""The test runner to be used by blinky, "sbt" or "bloop" (default "bloop")""")
        .maxOccurs(1),
      opt[String]("compileCommand")
        .valueName("<cmd>")
        .action { (compileCommand, config) =>
          config.add(_.modify(_.options.compileCommand).setTo(compileCommand))
        }
        .text("The compile command to be executed by sbt/bloop before the first run")
        .maxOccurs(1),
      opt[String]("testCommand")
        .valueName("<cmd>")
        .action { (testCommand, config) =>
          config.add(_.modify(_.options.testCommand).setTo(testCommand))
        }
        .text("The test command to be executed by sbt/bloop")
        .maxOccurs(1),
      opt[Boolean]("verbose")
        .valueName("<bool>")
        .action { (verbose, config) =>
          config.add(_.modify(_.options.verbose).setTo(verbose))
        }
        .text("If set, prints out debug information. Defaults to false")
        .maxOccurs(1),
      opt[Boolean]("onlyMutateDiff")
        .valueName("<bool>")
        .action { (onlyMutateDiff, config) =>
          config.add(_.modify(_.options.onlyMutateDiff).setTo(onlyMutateDiff))
        }
        .text("If set, only mutate added and edited files in git diff against the main branch")
        .maxOccurs(1),
      opt[String]("mainBranch")
        .valueName("<branch>")
        .action { (mainBranch, config) =>
          config.add(_.modify(_.options.mainBranch).setTo(mainBranch))
        }
        .text(
          "Sets the main branch to compare against when using --onlyMutateDiff (default 'main')"
        )
        .maxOccurs(1),
      opt[Boolean]("dryRun")
        .valueName("<bool>")
        .action { (dryRun, config) =>
          config.add(_.modify(_.options.dryRun).setTo(dryRun))
        }
        .text(
          "If set, apply mutations and compile the code but do not run the actual mutation testing"
        )
        .maxOccurs(1),
      opt[Duration]("maxRunningTime")
        .valueName("<duration>")
        .action { (maxRunningTime, config) =>
          config.add(_.modify(_.options.maxRunningTime).setTo(maxRunningTime))
        }
        .text("Maximum time allowed to run mutation tests")
        .maxOccurs(1),
      opt[Double]("mutationMinimum")
        .valueName("<decimal>")
        .action { (mutationMinimum, config) =>
          config.add(_.modify(_.options.mutationMinimum).setTo(mutationMinimum))
        }
        .text(
          "Minimum mutation score, value must be between 0 and 100, with one decimal place"
        )
        .maxOccurs(1),
      opt[Boolean]("failOnMinimum")
        .valueName("<bool>")
        .action { (failOnMinimum, config) =>
          config.add(_.modify(_.options.failOnMinimum).setTo(failOnMinimum))
        }
        .text(
          "If set, exits with non-zero code when the mutation score is below mutationMinimum value"
        )
        .maxOccurs(1),
      opt[(Int, Int)]("multiRun")(readMultiRun)
        .valueName("<job-index/amount-of-jobs>")
        .action { (fraction, config) =>
          config.add(_.modify(_.options.multiRun).setTo(fraction))
        }
        .text("Only test the mutants of the given index, 1 <= job-index <= amount-of-jobs")
        .maxOccurs(1),
      opt[Double]("timeoutFactor")
        .valueName("<decimal>")
        .action { (timeoutFactor, config) =>
          config.add(_.modify(_.options.timeoutFactor).setTo(timeoutFactor))
        }
        .text(s"Time factor for each mutant test")
        .maxOccurs(1),
      opt[Duration]("timeout")
        .valueName("<duration>")
        .action { (timeout, config) =>
          config.add(_.modify(_.options.timeout).setTo(timeout))
        }
        .text(s"Duration of additional flat timeout for each mutant test")
        .maxOccurs(1),
      opt[Boolean]("testInOrder")
        .valueName("<bool>")
        .action { (testInOrder, config) =>
          config.add(_.modify(_.options.testInOrder).setTo(testInOrder))
        }
        .text("If set, forces the mutants to be tested in order: 1,2,3,... (default false)")
        .maxOccurs(1),
      opt[String]("mutant")
        .valueName("<range>")
        .action { (mutantStr, config) =>
          config.add(
            _.modify(_.options.mutant).setTo(
              MutantRange.seqRangeDecoder.read(Conf.Str(mutantStr)).get
            )
          )
        }
        .validate(mutantStr =>
          MutantRange.seqRangeDecoder.read(Conf.Str(mutantStr)) match {
            case Configured.Ok(_)        => success
            case Configured.NotOk(error) => failure(error.msg)
          }
        )
        .text(s"Mutant indices to test. Defaults to 1-${Int.MaxValue}")
        .maxOccurs(1)
    )
  }
}
