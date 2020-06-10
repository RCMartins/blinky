package blinky.run

import ammonite.ops.pwd
import better.files.File
import blinky.TestSpec
import blinky.run.config.{MutationsConfigValidated, OptionsConfig, SimpleBlinkyConfig}
import blinky.run.modules.CliModule
import blinky.run.modules.TestModules.TestCliModule
import zio.test._
import zio.test.environment._
import zio.{ExitCode, UIO}

object RunTest extends TestSpec {

  private val path = pwd

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Run")(
      testM("something") {
        val zioInst = run(
          MutationsConfigValidated(
            projectPath = File("project"),
            filesToMutate = "src",
            filesToExclude = "",
            mutators = SimpleBlinkyConfig.default,
            options = OptionsConfig.default
          )
        )
        for {
          inst <- zioInst
        } yield testInstruction(
          inst,
          ???
        )
      }
    )

  private def run(
      config: MutationsConfigValidated,
      pwd: File = File(".")
  ): UIO[Instruction[ExitCode]] = {
    val cliEnv: CliModule = new CliModule {
      override val cliModule: CliModule.Service[Any] = new TestCliModule(pwd)
    }

    Run.run(config).provide(cliEnv)
  }

  private def testInstruction[A](
      actualInstruction: Instruction[A],
      expectationInstruction: TestInstruction[A]
  ): TestResult =
    ???

}
