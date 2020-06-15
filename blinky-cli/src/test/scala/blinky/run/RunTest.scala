package blinky.run

import ammonite.ops.{Path, pwd}
import better.files.File
import blinky.TestSpec
import blinky.run.TestInstruction._
import blinky.run.config.{MutationsConfigValidated, OptionsConfig, SimpleBlinkyConfig}
import blinky.run.modules.CliModule
import blinky.run.modules.TestModules.TestCliModule
import zio.test._
import zio.test.environment._
import zio.{ExitCode, UIO}

object RunTest extends TestSpec {

  private val path = pwd
  private val pathStr = path.toString

  private val originalProjectRoot = pwd
  private val originalProjectPath = originalProjectRoot
  private val cloneProjectTempFolder = Path("/tmp")
  private val gitFolder = Path("/project")
  private val cloneProjectBaseFolder = cloneProjectTempFolder / "project"
  private val projectRealPath = cloneProjectTempFolder / "project"

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Run")(
      suite("filesToMutateInstruction")(
        test("should return the correct instruction") {
          val inst = Run.filesToMutateInstruction(
            onlyMutateDiff = false,
            filesToMutate = "src",
            gitFolder,
            cloneProjectBaseFolder,
            projectRealPath,
            originalProjectPath,
            originalProjectRoot
          )

          println(inst)
          testInstruction(
            inst,
            for {
              _ <- testRunAsync(
                "git",
                Seq("ls-files", "--others", "--exclude-standard", "--cached"),
                Map.empty,
                originalProjectPath,
                mockResult = "file1" + System.lineSeparator() + "file2"
              )
            } yield Seq("all")
          )
        }
      ),
      testM("correct instruction") {
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
        } yield {
          println(inst)
          testInstruction(
            inst,
            for {
              _ <- testMakeTemporaryDirectory(Path("/tmp"))
              _ <-
                testRunAsync("git", Seq("rev-parse", "--show-toplevel"), Map.empty, path, pathStr)
            } yield testSucceed(???)
          )
        }
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

}
