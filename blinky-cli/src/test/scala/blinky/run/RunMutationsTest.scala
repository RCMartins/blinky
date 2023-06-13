package blinky.run

import better.files.File
import blinky.TestSpec._
import blinky.run.TestInstruction._
import blinky.run.config.OptionsConfig
import os.Path
import zio.test._
import zio.{ExitCode, Scope, ZIO, ZLayer}

object RunMutationsTest extends ZIOSpecDefault {

  private lazy val projectPath: Path = Path(getFilePath("some-project"))
  private lazy val someException = SomeException("some exception")

  private lazy val emptyMutantsOutputFile: String = getFilePath("mutants/blinky-empty.mutants")
  private lazy val validMutantsOutputFile: String = getFilePath("mutants/blinky1.mutants")
  private lazy val validMutantsOutputText: String = File(validMutantsOutputFile).contentAsString
  private val mockDiffString = "mock-diff-string"

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunMutations")(
      runSuite,
      initializeRunMutationsSuite,
      initializeRunInitialCompileSuite,
      initializeRunInitialTestsSuite,
//      runMutantSuite,
    )

  private def runSuite: Spec[Any, Nothing] =
    suite("run")(
      test("return success with a message if no mutants found") {
        testRunMutations(
          _.run(projectPath, emptyMutantsOutputFile, OptionsConfig.default),
          TestReadFile(
            Path(emptyMutantsOutputFile),
            Left(someException),
            TestPrintErrorLine(
              s"""Blinky failed to load mutants file:
                 |$emptyMutantsOutputFile
                 |""".stripMargin,
              TestPrintLine(
                "0 mutants found in 0 scala files.",
                TestPrintLine(
                  "Try changing the mutation settings.",
                  TestReturn(ExitCode.success),
                )
              )
            )
          )
        )
      }
    ).provideShared(provideRunner(createRunner()))

  private def initializeRunMutationsSuite: Spec[Any, Nothing] =
    suite("initializeRunMutations")(
      test("runner.initializeRunner fails") {
        testRunMutations(
          _.run(projectPath, validMutantsOutputFile, OptionsConfig.default),
          TestReadFile(
            Path(validMutantsOutputFile),
            Right(validMutantsOutputText),
            TestPrintLine(
              "2 mutants found in 1 scala files.",
              TestPrintLine(
                s"initializeRunner: $projectPath",
                TestPrintErrorLine(
                  "blinky.TestSpec$SomeException: some exception",
                  TestPrintErrorLine(
                    "There were errors while initializing blinky!",
                    TestReturn(ExitCode.failure),
                  )
                )
              )
            )
          )
        )
      }
    ).provideShared(provideRunner(createRunner(initializeRunnerResult = Left(someException))))

  private def initializeRunInitialCompileSuite: Spec[Any, Nothing] =
    suite("runInitialCompile")(
      test("runner.initialCompile fails") {
        testRunMutations(
          _.run(projectPath, validMutantsOutputFile, OptionsConfig.default),
          TestReadFile(
            Path(validMutantsOutputFile),
            Right(validMutantsOutputText),
            TestPrintLine(
              "2 mutants found in 1 scala files.",
              TestPrintLine(
                s"initializeRunner: $projectPath",
                TestPrintLine(
                  "Running tests with original config",
                  TestPrintLine(
                    s"initialCompile: $projectPath",
                    TestPrintErrorLine(
                      "blinky.TestSpec$SomeException: some exception",
                      TestPrintErrorLine(
                        s"""There are compile errors after applying the Blinky rule.
                           |This could be because Blinky is not configured correctly.
                           |Make sure compileCommand is set.
                           |If you think it's due to a bug in Blinky please to report in:
                           |https://github.com/RCMartins/blinky/issues/new""".stripMargin,
                        TestReturn(ExitCode.failure),
                      )
                    )
                  )
                )
              )
            )
          )
        )
      }
    ).provideShared(provideRunner(createRunner(initialCompileResult = Left(someException))))

  private def initializeRunInitialTestsSuite: Spec[Any, Nothing] =
    suite("runInitialTests")(
      test("runner.vanillaTestRun fails") {
        testRunMutations(
          _.run(projectPath, validMutantsOutputFile, OptionsConfig.default),
          TestReadFile(
            Path(validMutantsOutputFile),
            Right(validMutantsOutputText),
            TestPrintLine(
              "2 mutants found in 1 scala files.",
              TestPrintLine(
                s"initializeRunner: $projectPath",
                TestPrintLine(
                  "Running tests with original config",
                  TestPrintLine(
                    s"initialCompile: $projectPath",
                    TestPrintLine(
                      s"vanillaTestRun: $projectPath",
                      TestPrintErrorLine(
                        """Tests failed. No mutations will run until this is fixed.
                          |This could be because Blinky is not configured correctly.
                          |Make sure testCommand is set.
                          |
                          |blinky.TestSpec$SomeException: some exception
                          |""".stripMargin,
                        TestReturn(ExitCode.failure),
                      )
                    )
                  )
                )
              )
            )
          )
        )
      }.provideShared(provideRunner(createRunner(vanillaTestRunResult = Left(someException)))),
      test("runner.vanillaTestRun works and it's running in dryRun mode") {
        testRunMutations(
          _.run(
            projectPath,
            validMutantsOutputFile,
            OptionsConfig.default.copy(verbose = true, dryRun = true)
          ),
          TestReadFile(
            Path(validMutantsOutputFile),
            Right(validMutantsOutputText),
            TestPrintLine(
              "2 mutants found in 1 scala files.",
              TestPrintLine(
                s"initializeRunner: $projectPath",
                TestPrintLine(
                  "Running tests with original config",
                  TestPrintLine(
                    s"initialCompile: $projectPath",
                    TestPrintLine(
                      s"vanillaTestRun: $projectPath",
                      TestPrintLine(
                        greenText("Original tests passed..."),
                        TestPrintLine(
                          "some-test-result",
                          TestPrintLineAny( // TODO: check for a better way to test this
                            TestPrintLine(
                              s"""
                                 |${greenText("In dryRun mode. Everything worked correctly.")}
                                 |If you want to run it again with mutations active use --dryRun=false
                                 |""".stripMargin,
                              TestReturn(ExitCode.success),
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )
      }.provideShared(
        provideRunner(createRunner(vanillaTestRunResult = Right("some-test-result")))
      ),
    )

  private def testRunMutations[A](
      actualInstruction: RunMutations => Instruction[A],
      expectationInstruction: TestInstruction[A]
  ): ZIO[RunMutations, Nothing, TestResult] =
    for {
      instance <- ZIO.service[RunMutations]
    } yield testInstruction(
      actualInstruction(instance),
      expectationInstruction
    )

  private def provideRunner(
      runner: MutationsRunner
  ): ZLayer[Any, Nothing, MutationsRunner with PrettyDiff with RunMutations] =
    ZLayer.succeed(runner) >+> dummyPrettyDiff >+> RunMutations.live

  private def createRunner(
      initializeRunnerResult: Either[Throwable, Unit] = Right(()),
      initialCompileResult: Either[Throwable, Unit] = Right(()),
      fullTestCommandResult: String = "mock-full-test-command-result",
      vanillaTestRunResult: Either[Throwable, String] = Right("mock-vanilla-test-run-result"),
  ): MutationsRunner = new MutationsRunner {
    override def initializeRunner(projectPath: Path): Instruction[Either[Throwable, Unit]] =
      Instruction.printLine(s"initializeRunner: $projectPath").map(_ => initializeRunnerResult)

    override def initialCompile(
        projectPath: Path,
        compileCommand: String
    ): Instruction[Either[Throwable, Unit]] =
      Instruction.printLine(s"initialCompile: $projectPath").map(_ => initialCompileResult)

    override def fullTestCommand(testCommand: String): String =
      fullTestCommandResult

    override def vanillaTestRun(
        projectPath: Path,
        testCommand: String
    ): Instruction[Either[Throwable, String]] =
      Instruction.printLine(s"vanillaTestRun: $projectPath").map(_ => vanillaTestRunResult)

    override def cleanRunnerAfter(
        projectPath: Path,
        results: List[(Int, RunResult)]
    ): Instruction[Unit] =
      Instruction.empty
  }

  private def dummyPrettyDiff: ZLayer[Any, Nothing, PrettyDiff] =
    ZLayer.succeed(new PrettyDiff {
      override def prettyDiff(
          diffLinesStr: String,
          fileName: String,
          projectPath: String,
          color: Boolean
      ): String = mockDiffString
    })

}
