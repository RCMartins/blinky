package blinky.run

import blinky.internal.MutantFile
import blinky.run.Instruction._
import blinky.run.Utils._
import blinky.run.config.OptionsConfig
import blinky.v0.BlinkyConfig
import os.Path
import zio.ExitCode
import zio.json.DecoderOps

import scala.util.Random

object TestMutationsBloop {

  private val defaultEnvArgs: Map[String, String] =
    Map("BLINKY" -> "true", "BLOOP_TRACING" -> "false")

  def run(
      projectPath: Path,
      blinkyConfig: BlinkyConfig,
      options: OptionsConfig
  ): Instruction[ExitCode] = {

    for {
      mutationReport <-
        readFile(Path(blinkyConfig.mutantsOutputFile)).flatMap {
          case Left(_) =>
            printErrorLine(
              s"""Blinky failed to load mutants file:
                 |${blinkyConfig.mutantsOutputFile}
                 |""".stripMargin
            ).map(_ => List.empty[MutantFile])
          case Right(fileData) =>
            val (mutantsParsed, errors) =
              fileData
                .split("\n")
                .filter(_.nonEmpty)
                .map(_.fromJson[MutantFile])
                .toList
                .partition(_.isRight)
            if (errors.nonEmpty)
              printErrorLine(
                s"""Blinky failed to parse mutants file:
                   |${blinkyConfig.mutantsOutputFile}
                   |${errors.mkString("\n")}
                   |""".stripMargin
              ).map(_ => List.empty[MutantFile])
            else
              succeed(
                mutantsParsed
                  .collect { case Right(mutant) => mutant }
                  .filter { mutant =>
                    val (numerator, denominator) = options.multiRun
                    (mutant.id % denominator) == (numerator - 1)
                  }
              )
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
            _ <- runStream(
              "sbt",
              Seq("bloopInstall"),
              envArgs = defaultEnvArgs,
              path = projectPath
            )
            _ <- printLine("Running tests with original config")
            compileResult <- runResultEither(
              "bloop",
              Seq("compile", escapeString(options.compileCommand)),
              envArgs = defaultEnvArgs,
              path = projectPath
            )
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
                  vanillaTestResult <- runBashEither(
                    s"bloop test ${escapeString(testCommand)}",
                    envArgs = defaultEnvArgs,
                    path = projectPath
                  )

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
      results <- runMutations(projectPath, options, originalTestTime, mutationsToTest, initialTime)
      totalTime = System.currentTimeMillis() - initialTime

      result <-
        ConsoleReporter.reportMutationResult(results, totalTime, numberOfMutants, options)
      _ <- killBloopIfNecessary(projectPath, results)
    } yield
      if (result)
        ExitCode.success
      else
        ExitCode.failure
  }

  private def killBloopIfNecessary(
      projectPath: Path,
      results: List[(Int, RunResult)]
  ): Instruction[Unit] =
    if (results.exists(_._2 == RunResult.Timeout))
      runBashEither("bloop exit", path = projectPath).flatMap {
        case Left(error) =>
          printErrorLine(
            s"""Failed to run 'bloop exit' after a mutation timeout.
               |$error""".stripMargin
          )
        case Right(_) =>
          succeed(())
      }
    else
      succeed(())

  def runMutations(
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
            mutantResult <- runMutant(projectPath, options, originalTestTime, mutant)
            result <- loop(othersMutants)
          } yield mutantResult :: result
      }

    loop(initialMutants)
  }

  def runMutant(
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutant: MutantFile
  ): Instruction[(Int, RunResult)] = {
    val id = mutant.id
    val time = System.currentTimeMillis()

    for {
      testResult <- runInBloop(projectPath, options, originalTestTime, mutant)

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

  def runInBloop(
      projectPath: Path,
      options: OptionsConfig,
      originalTestTime: Long,
      mutant: MutantFile
  ): Instruction[RunResult] = {
    val prints =
      if (options.verbose)
        for {
          _ <- printLine(
            s"> [BLINKY_MUTATION_${mutant.id}=1] " +
              s"""bash -c "bloop test ${escapeString(options.testCommand)}""""
          )
          _ <- printLine(
            prettyDiff(mutant.diff, mutant.fileName, projectPath.toString, color = true)
          )
          _ <- printLine("--v--" * 5)
        } yield ()
      else
        empty

    val runBloopResult: Instruction[Either[Throwable, TimeoutResult]] =
      prints.flatMap(_ =>
        runBashTimeout(
          s"bloop test ${escapeString(options.testCommand)}",
          envArgs = defaultEnvArgs + (s"BLINKY_MUTATION_${mutant.id}" -> "1"),
          timeout = (originalTestTime * options.timeoutFactor + options.timeout.toMillis).toLong,
          path = projectPath
        )
      )

    runBloopResult.map {
      case Right(TimeoutResult.Ok)      => RunResult.MutantSurvived
      case Right(TimeoutResult.Timeout) => RunResult.Timeout
      case Left(_)                      => RunResult.MutantKilled
    }
  }
}
