package blinky.run

import blinky.internal.MutantFile
import blinky.run.Instruction._
import blinky.run.Utils._
import blinky.run.config.{OptionsConfig, TestRunnerType}
import blinky.v0.BlinkyConfig
import os.Path
import zio.ExitCode
import zio.json.DecoderOps

import scala.util.Random

object TestMutations {

  def run(
      projectPath: Path,
      blinkyConfig: BlinkyConfig,
      options: OptionsConfig
  ): Instruction[ExitCode] = {
    for {
      mutationReport <- readFile(Path(blinkyConfig.mutantsOutputFile)).flatMap {
        case Left(_) =>
          printErrorLine(
            s"""Blinky failed to load mutants file:
               |${blinkyConfig.mutantsOutputFile}
               |""".stripMargin
          ).map(_ => List.empty[MutantFile])
        case Right(fileData) =>
          val (numerator, denominator) = options.multiRun
          succeed(
            fileData
              .split("\n")
              .filter(_.nonEmpty)
              .map(_.fromJson[MutantFile])
              .toList
              .collect {
                case Right(mutant) if (mutant.id % denominator) == (numerator - 1) =>
                  mutant
              }
          )
      }

      runner = options.testRunner match {
        case TestRunnerType.SBT   => new TestMutationsSBT(projectPath)
        case TestRunnerType.Bloop => new TestMutationsBloop(projectPath)
      }
      testCommand = options.testCommand
      numberOfMutants = mutationReport.length

      _ <- {
        val numberOfFilesWithMutants = mutationReport.view.groupBy(_.fileName).size
        printLine(s"$numberOfMutants mutants found in $numberOfFilesWithMutants scala files.")
      }

      testResult <-
        if (numberOfMutants == 0)
          printLine("Try changing the mutation settings.").map(_ => ExitCode.success)
        else
          for {
            _ <- runner.initializeRunner()
            _ <- printLine("Running tests with original config")
            compileResult <- runner.initialCompile(options.compileCommand)
            res <- compileResult match {
              case Left(error) =>
                val newIssueLink = "https://github.com/RCMartins/blinky/issues/new"
                printErrorLine(error.toString)
                  .flatMap(_ =>
                    printErrorLine(
                      s"""There are compile errors after applying the Blinky rule.
                         |This could be because Blinky is not configured correctly.
                         |Make sure compileCommand is set.
                         |If you think it's due to a bug in Blinky please to report in:
                         |$newIssueLink""".stripMargin
                    )
                  )
                  .map(_ => ExitCode.failure)
              case Right(_) =>
                for {
                  originalTestInitialTime <- succeed(System.currentTimeMillis())
                  vanillaTestResult <- runner.vanillaTestRun(testCommand)
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
                        _ <- conditional(options.verbose)(printLine(result))
                        originalTestTime <- succeed(
                          System.currentTimeMillis() - originalTestInitialTime
                        )
                        _ <- conditional(options.verbose)(
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
                              runner,
                              projectPath,
                              options,
                              originalTestTime,
                              numberOfMutants,
                              mutationReport
                            )
                      } yield res
                  }
                } yield res
            }
          } yield res
    } yield testResult
  }

  def runMutationsSetup(
      runner: TestMutationsRunner,
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      numberOfMutants: Int,
      mutationReport: List[MutantFile]
  ): Instruction[ExitCode] = {
    val mutationsToTest =
      if (
        originalTestTime * numberOfMutants >= options.maxRunningTime.toMillis && !options.testInOrder
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
        runner,
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

  def runMutations(
      runner: TestMutationsRunner,
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      initialMutants: List[MutantFile],
      initialTime: Long
  ): Instruction[List[(Int, RunResult)]] = {
    def loop(mutants: List[MutantFile]): Instruction[List[(Int, RunResult)]] =
      mutants match {
        case Nil =>
          succeed(Nil)
        case _ if System.currentTimeMillis() - initialTime > options.maxRunningTime.toMillis =>
          printLine(
            s"Timed out - maximum of ${options.maxRunningTime} " +
              s"(this can be changed with --maxRunningTime parameter)"
          ).map(_ => Nil)
        case mutant :: othersMutants =>
          for {
            mutantResult <- runMutant(runner, projectPath, options, originalTestTime, mutant)
            result <- loop(othersMutants)
          } yield mutantResult :: result
      }

    loop(initialMutants)
  }

  def runMutant(
      runner: TestMutationsRunner,
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutant: MutantFile
  ): Instruction[(Int, RunResult)] = {
    val id = mutant.id
    val time = System.currentTimeMillis()

    for {
      testResult <- runner.runMutant(projectPath, options, originalTestTime, mutant)

      _ <- testResult match {
        case RunResult.MutantSurvived =>
          printLine(red(s"Mutant #$id was not killed!")).flatMap(_ =>
            if (!options.verbose)
              printLine(
                prettyDiff(
                  mutant.diff,
                  mutant.fileName,
                  projectPath.toString,
                  color = true
                )
              )
            else
              empty
          )
        case RunResult.MutantKilled =>
          printLine(green(s"Mutant #$id was killed."))
        case RunResult.Timeout =>
          printLine(green(s"Mutant #$id timeout."))
      }

      _ <-
        if (options.verbose)
          printLine(s"time: ${System.currentTimeMillis() - time}").flatMap(_ => printLine("-" * 40))
        else
          empty

    } yield id -> testResult
  }

}
