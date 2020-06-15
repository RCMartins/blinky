package blinky.run

import ammonite.ops.Path
import blinky.run.Instruction._
import blinky.run.Utils._
import blinky.run.config.OptionsConfig
import blinky.v0.BlinkyConfig
import play.api.libs.json.Json
import zio.ExitCode

import scala.util.Random

object TestMutationsBloop {
  def run(
      projectPath: Path,
      blinkyConfig: BlinkyConfig,
      options: OptionsConfig
  ): Instruction[ExitCode] = {

    for {

      mutationReport <- readFile(Path(blinkyConfig.mutantsOutputFile))
        .map(
          _.split("\n")
            .filter(_.nonEmpty)
            .map(Json.parse(_).as[Mutant])
            .toList
        )
      testCommand = options.testCommand
      numberOfMutants = mutationReport.length

      runInBloop: (Mutant => Instruction[Boolean]) = { (mutant: Mutant) =>
        val prints =
          conditional(options.verbose)(
            for {
              _ <- printLine(
                s"""> [BLINKY_MUTATION_${mutant.id}=1] bash -c "bloop test ${escapeString(
                  testCommand
                )}""""
              )
              _ <- printLine(
                prettyDiff(mutant.diff, mutant.fileName, projectPath.toString, color = true)
              )
              _ <- printLine("--v--" * 5)
            } yield ()
          )

        prints.flatMap(_ =>
          runAsyncSuccess(
            "bloop",
            Seq(
              "test",
              escapeString(testCommand)
            ),
            Map(s"BLINKY_MUTATION_${mutant.id}" -> "1"),
            projectPath
          )
        )
      }

      runMutant: (Mutant => Instruction[(Int, Boolean)]) = { (mutant: Mutant) =>
        val id = mutant.id
        for {
          time <- succeed(System.currentTimeMillis())
          testResult <- runInBloop(mutant)

          _ <-
            if (testResult)
              printLine(red(s"Mutant #$id was not killed!")).flatMap(_ =>
                conditional(!options.verbose)(
                  printLine(
                    prettyDiff(
                      mutant.diff,
                      mutant.fileName,
                      projectPath.toString,
                      color = true
                    )
                  )
                )
              )
            else
              printLine(green(s"Mutant #$id was killed."))

          _ <- conditional(options.verbose)(
            printLine(s"time: ${System.currentTimeMillis() - time}").flatMap(_ =>
              printLine("-" * 40)
            )
          )

        } yield id -> !testResult
      }

      runMutations: (List[Mutant] => Long => Instruction[List[(Int, Boolean)]]) = {
        (initialMutants: List[Mutant]) => (initialTime: Long) =>
          def loop(mutants: List[Mutant]): Instruction[List[(Int, Boolean)]] =
            mutants match {
              case Nil =>
                succeed(Nil)
              case _
                  if System.currentTimeMillis() - initialTime > options.maxRunningTime.toMillis =>
                printLine(
                  s"Timed out - maximum of ${options.maxRunningTime} " +
                    s"(this can be changed with --maxRunningTime parameter)"
                ).map(_ => Nil)
              case mutant :: othersMutants =>
                runMutant(mutant).flatMap(mutantResult =>
                  loop(othersMutants).map(mutantResult :: _)
                )
            }

          loop(initialMutants)
      }

      runMutationsSetup: (Long => Instruction[ExitCode]) = { (originalTestTime: Long) =>
        val mutationsToTest =
          if (originalTestTime * numberOfMutants >= options.maxRunningTime.toMillis)
            Random.shuffle(mutationReport)
          else
            mutationReport

        for {
          _ <- printLine(
            s"Running the same tests on mutated code (maximum of ${options.maxRunningTime})"
          )

          initialTime = System.currentTimeMillis()
          results <- runMutations(mutationsToTest)(initialTime)
          totalTime = System.currentTimeMillis() - initialTime

          result <-
            ConsoleReporter.reportMutationResult(results, totalTime, numberOfMutants, options)
        } yield
          if (result)
            ExitCode.success
          else
            ExitCode.failure
      }

      _ <- printLine(s"$numberOfMutants mutants found.")

      testResult <-
        if (numberOfMutants == 0)
          printLine("Try changing the mutation settings.").map(_ => ExitCode.success)
        else
          for {
            _ <- runSync("sbt", Seq("bloopInstall"), projectPath)
            _ <- printLine("Running tests with original config")
            compileResult <- runAsyncEither(
              "bloop",
              Seq("compile", escapeString(options.compileCommand)),
              path = projectPath
            )
            res <- compileResult match {
              case Left(error) =>
                val newIssueLink = "https://github.com/RCMartins/blinky/issues/new"
                printErrorLine(error)
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
                  vanillaTestResult <- runAsyncEither(
                    "bloop",
                    Seq(s"test", escapeString(testCommand)),
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
                            runMutationsSetup(originalTestTime)
                      } yield res
                  }
                } yield res
            }
          } yield res
    } yield testResult
  }
}
