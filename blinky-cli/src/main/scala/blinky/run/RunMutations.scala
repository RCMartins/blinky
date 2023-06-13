package blinky.run

import blinky.internal.MutantFile
import blinky.run.Instruction._
import blinky.run.Setup.defaultEnvArgs
import blinky.run.Utils._
import blinky.run.config.OptionsConfig
import os.Path
import zio.{ExitCode, ZIO, ZLayer}
import zio.json.DecoderOps

import scala.util.Random

class RunMutations(
    runner: MutationsRunner,
    prettyDiff: PrettyDiff
) {

  def run(
      projectPath: Path,
      mutantsOutputFile: String,
      options: OptionsConfig,
  ): Instruction[ExitCode] =
    for {
      mutationReport <- readFile(Path(mutantsOutputFile)).flatMap {
        case Left(_) =>
          printErrorLine(
            s"""Blinky failed to load mutants file:
               |$mutantsOutputFile
               |""".stripMargin
          ).map(_ => Seq.empty[MutantFile])
        case Right(fileData) =>
          val (numerator, denominator) = options.multiRun
          succeed(
            fileData
              .split("\n")
              .filter(_.nonEmpty)
              .map(_.fromJson[MutantFile])
              .toSeq
              .collect {
                case Right(mutant) if (mutant.id % denominator) == (numerator - 1) =>
                  mutant
              }
          )
      }
      numberOfMutants = mutationReport.size
      _ <- {
        val numberOfFilesWithMutants = mutationReport.map(_.fileName).distinct.size
        printLine(s"$numberOfMutants mutants found in $numberOfFilesWithMutants scala files.")
      }
      testResult <-
        if (numberOfMutants == 0)
          printLine("Try changing the mutation settings.").map(_ => ExitCode.success)
        else
          initializeRunMutations(projectPath, options, mutationReport)
    } yield testResult

  private def initializeRunMutations(
      projectPath: Path,
      options: OptionsConfig,
      mutationReport: Seq[MutantFile],
  ): Instruction[ExitCode] =
    runner.initializeRunner(projectPath).flatMap {
      case Left(error) =>
        printErrorLine(error.toString) *>
          printErrorLine("There were errors while initializing blinky!")
            .map(_ => ExitCode.failure)
      case Right(_) =>
        runInitialCompile(
          projectPath,
          options,
          mutationReport,
        )
    }

  private def runInitialCompile(
      projectPath: Path,
      options: OptionsConfig,
      mutationReport: Seq[MutantFile],
  ): Instruction[ExitCode] =
    for {
      _ <- printLine("Running tests with original config")
      compileResult <- runner.initialCompile(projectPath, options.compileCommand)
      res <- compileResult match {
        case Left(error) =>
          val newIssueLink = "https://github.com/RCMartins/blinky/issues/new"
          printErrorLine(error.toString) *>
            printErrorLine(
              s"""There are compile errors after applying the Blinky rule.
                 |This could be because Blinky is not configured correctly.
                 |Make sure compileCommand is set.
                 |If you think it's due to a bug in Blinky please to report in:
                 |$newIssueLink""".stripMargin
            ).map(_ => ExitCode.failure)
        case Right(_) =>
          runInitialTests(projectPath, options, mutationReport)
      }
    } yield res

  private def runInitialTests(
      projectPath: Path,
      options: OptionsConfig,
      mutationReport: Seq[MutantFile],
  ): Instruction[ExitCode] =
    for {
      originalTestInitialTime <- succeed(System.currentTimeMillis())
      vanillaTestResult <- runner.vanillaTestRun(projectPath, options.testCommand)
      res <- vanillaTestResult match {
        case Left(error) =>
          printErrorLine(
            s"""Tests failed. No mutations will run until this is fixed.
               |This could be because Blinky is not configured correctly.
               |Make sure testCommand is set.
               |
               |$error
               |""".stripMargin
          ).map(_ => ExitCode.failure)
        case Right(result) =>
          for {
            _ <- printLine(green("Original tests passed..."))
            _ <- when(options.verbose)(printLine(result))
            originalTestTime = System.currentTimeMillis() - originalTestInitialTime
            _ <- when(options.verbose)(
              printLine(green("time: " + originalTestTime))
            )
            res <-
              if (options.dryRun)
                printLine(
                  s"""
                     |${green("In dryRun mode. Everything worked correctly.")}
                     |If you want to run it again with mutations active use --dryRun=false
                     |""".stripMargin
                ).map(_ => ExitCode.success)
              else
                runMutationsSetup(
                  projectPath,
                  options,
                  originalTestTime,
                  mutationReport,
                )
          } yield res
      }
    } yield res

  private def runMutationsSetup(
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutationReport: Seq[MutantFile]
  ): Instruction[ExitCode] = {
    val numberOfMutants: Int = mutationReport.size
    val mutationsToTest =
      if (
        !options.testInOrder && originalTestTime * numberOfMutants >= options.maxRunningTime.toMillis
      )
        Random.shuffle(mutationReport)
      else
        mutationReport

    for {
      _ <- printLine(
        s"Running the same tests on mutated code (maximum of ${options.maxRunningTime})"
      )

      initialTime = System.currentTimeMillis()
      results <- runMutations(
        projectPath,
        options,
        originalTestTime,
        mutationsToTest,
        initialTime
      )
      totalTime = System.currentTimeMillis() - initialTime

      result <-
        ConsoleReporter.reportMutationResult(results, totalTime, numberOfMutants, options)
      _ <- runner.cleanRunnerAfter(projectPath, results)
    } yield
      if (result)
        ExitCode.success
      else
        ExitCode.failure
  }

  private def runMutations(
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      initialMutants: Seq[MutantFile],
      initialTime: Long
  ): Instruction[List[(Int, RunResult)]] = {
    def loop(mutants: Seq[MutantFile], index: Int): Instruction[List[(Int, RunResult)]] =
      if (index > mutants.size)
        succeed(Nil)
      else if (System.currentTimeMillis() - initialTime > options.maxRunningTime.toMillis)
        printLine(
          s"Timed out - maximum of ${options.maxRunningTime} " +
            s"(this can be changed with --maxRunningTime parameter)"
        ).map(_ => Nil)
      else
        for {
          mutantResult <- runMutant(projectPath, options, originalTestTime, mutants(index))
          result <- loop(mutants, index + 1)
        } yield mutantResult :: result

    loop(initialMutants, index = 0)
  }

  private def runMutant(
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutant: MutantFile
  ): Instruction[(Int, RunResult)] = {
    val id = mutant.id
    val time = System.currentTimeMillis()

    for {
      testResult <- runMutantCommandLine(runner, projectPath, options, originalTestTime, mutant)
      _ <- testResult match {
        case RunResult.MutantSurvived =>
          printLine(red(s"Mutant #$id was not killed!")) *>
            when(!options.verbose)(
              printLine(
                prettyDiff.prettyDiff(
                  mutant.diff,
                  mutant.fileName,
                  projectPath.toString,
                  color = true
                )
              )
            )
        case RunResult.MutantKilled =>
          printLine(green(s"Mutant #$id was killed."))
        case RunResult.Timeout =>
          printLine(green(s"Mutant #$id timeout."))
      }
      _ <-
        when(options.verbose)(
          printLine(s"time: ${System.currentTimeMillis() - time}") *>
            printLine("-" * 40)
        )
    } yield id -> testResult
  }

  private def runMutantCommandLine(
      runner: MutationsRunner,
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutant: MutantFile,
  ): Instruction[RunResult] = {
    val prints: Instruction[Unit] =
      when(options.verbose)(
        for {
          _ <- printLine(
            s"> [BLINKY_MUTATION_${mutant.id}=1] " +
              s"""bash -c "${runner.fullTestCommand(options.testCommand)}""""
          )
          _ <- printLine(
            prettyDiff.prettyDiff(mutant.diff, mutant.fileName, projectPath.toString, color = true)
          )
          _ <- printLine("--v--" * 5)
        } yield ()
      )

    val runResult: Instruction[Either[Throwable, TimeoutResult]] =
      prints *>
        runBashTimeout(
          runner.fullTestCommand(options.testCommand),
          envArgs = defaultEnvArgs + (s"BLINKY_MUTATION_${mutant.id}" -> "1"),
          timeout = (originalTestTime * options.timeoutFactor + options.timeout.toMillis).toLong,
          path = projectPath
        )

    runResult.map {
      case Right(TimeoutResult.Ok)      => RunResult.MutantSurvived
      case Right(TimeoutResult.Timeout) => RunResult.Timeout
      case Left(_)                      => RunResult.MutantKilled
    }
  }

}

object RunMutations {

  def live: ZLayer[MutationsRunner with PrettyDiff, Nothing, RunMutations] =
    ZLayer(
      for {
        runner <- ZIO.service[MutationsRunner]
        prettyDiff <- ZIO.service[PrettyDiff]
      } yield new RunMutations(runner, prettyDiff)
    )

}
