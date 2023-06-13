package blinky.run

import better.files.File
import blinky.TestSpec._
import blinky.internal.MutantFile
import blinky.run.TestInstruction._
import blinky.run.config.OptionsConfig
import os.Path
import zio.test._
import zio.{ExitCode, Scope, ZIO, ZLayer}

import scala.concurrent.duration.DurationInt

object RunMutationsTest extends ZIOSpecDefault {

  private lazy val projectPath: Path = Path(getFilePath("some-project"))
  private lazy val emptyMutantsOutputFile: String = getFilePath("blinky-empty.mutants")
  private lazy val someException = SomeException("some exception")

  private lazy val validMutantsOutputFile: String = getFilePath("blinky1.mutants")
  private lazy val validMutantsOutputText: String = File(validMutantsOutputFile).contentAsString
  private val mockDiffString = "mock-diff-string"

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunMutations")(
      test("return success with a message if no mutants found") {
        testRunMutations(
          _.run(
            projectPath,
            emptyMutantsOutputFile,
            OptionsConfig.default,
          ),
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
                ),
              ),
            ),
          )
        )
      }.provideSome[PrettyDiff](ZLayer.succeed(RunMutationsSBT) >+> RunMutations.live),
      runMutantSuite,
    ).provideSomeLayerShared(dummyPrettyDiff)

  private def runMutantSuite: Spec[PrettyDiff, Nothing] =
//    def baseRunMutantResult(
//        result: Either[Throwable, TimeoutResult],
//        expected: RunResult
//    ): TestInstruction[RunResult] =
//      TestRunResultTimeout(
//        "bash",
//        Seq("-c", "sbt  testOnly -- -z \\\"test name\\\""),
//        Map("BLINKY" -> "true", "BLINKY_MUTATION_1" -> "1"),
//        4000,
//        projectPath,
//        result,
//        TestReturn(expected)
//      )
//
//    val options: OptionsConfig =
//      OptionsConfig.default.copy(
//        testCommand = "testOnly -- -z \"test name\"",
//        timeoutFactor = 2.0,
//        timeout = 2000.millis,
//      )
//
//    val mutantFile: MutantFile = MutantFile(
//      1,
//      """-  val foo1 = 1 + 1
//        |+  val foo1 = 1 - 1
//        |""".stripMargin,
//      "SomeFile.scala",
//      "",
//      ""
//    )

    suite("runMutant")(
//      test("run tests with an active mutant when verbose=false") {
//        check(
//          Gen.elements(
//            Right(TimeoutResult.Ok) -> RunResult.MutantSurvived,
//            Right(TimeoutResult.Timeout) -> RunResult.Timeout,
//            Left(someException) -> RunResult.MutantKilled,
//          )
//        ) { case (result, expected) =>
//          testRunMutations(
//            _.runMutant(
//              projectPath = projectPath,
//              options = options,
//              originalTestTime = 1000L,
//              mutant = mutantFile,
//            ),
//            baseRunMutantResult(result, expected)
//          )
//        }
//      },
//      test("run tests with an active mutant when verbose=true") {
//        check(
//          Gen.elements(
//            Right(TimeoutResult.Ok) -> RunResult.MutantSurvived,
//            Right(TimeoutResult.Timeout) -> RunResult.Timeout,
//            Left(someException) -> RunResult.MutantKilled,
//          )
//        ) { case (result, expected) =>
//          testRunMutations(
//            _.runMutant(
//              projectPath = projectPath,
//              options = options.copy(verbose = true),
//              originalTestTime = 1000L,
//              mutant = mutantFile,
//            ),
//            TestPrintLine(
//              """> [BLINKY_MUTATION_1=1] bash -c "sbt sbt  testOnly -- -z \"test name\"""",
//              TestPrintLine(
//                """SomeFile.scala
//                  |$[36m@@ -3,7 +3,7 @@ package test$[0m
//                  |  3       object GeneralSyntax4 {
//                  |  4         val some1 = Some("value")
//                  |  5   ####
//                  |$[31m  6      -  val foo1 = 1 + 1$[0m
//                  |$[32m     6   +  val foo1 = 1 - 1$[0m
//                  |  7  7####
//                  |  8  8      val some2 = Some("value")
//                  |  9  9   #""".stripMargin
//                  .replace("#", " ")
//                  .replace("$", "\u001B")
//                  .replace("\r", ""),
//                TestPrintLine(
//                  "--v----v----v----v----v--",
//                  baseRunMutantResult(result, expected)
//                )
//              )
//            )
//          )
//        }
//      },
    ).provideSomeShared(ZLayer.succeed(createRunner()) >+> RunMutations.live)

  def testRunMutations[A](
      actualInstruction: RunMutations => Instruction[A],
      expectationInstruction: TestInstruction[A]
  ): ZIO[RunMutations, Nothing, TestResult] =
    for {
      instance <- ZIO.service[RunMutations]
    } yield testInstruction(
      actualInstruction(instance),
      expectationInstruction
    )

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
      Instruction.printLine(s"initialCompile $projectPath").map(_ => initialCompileResult)

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
