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

  private val path: Path = pwd
//  private val pathStr = path.toString

  private val originalProjectRoot: Path = pwd
  private val originalProjectPath: Path = originalProjectRoot
  private val cloneProjectTempFolder: Path = Path("/tmp")
  private val gitFolder: Path = Path("/test-project")
  private val cloneProjectBaseFolder: Path = cloneProjectTempFolder / "test-project"
  private val projectRealPath: Path = originalProjectPath / "test-project"

  private val lineSeparator = System.lineSeparator()

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Run")(
      suite("filesToMutateInstruction")(
        test("instruction when onlyMutateDiff = false") {
          val inst = filesToMutateInstruction(
            onlyMutateDiff = false,
            filesToMutate = "src"
          )

          testInstruction(
            inst,
            for {
              _ <- testRunAsync(
                "git",
                Seq("ls-files", "--others", "--exclude-standard", "--cached"),
                Map.empty,
                originalProjectPath,
                mockResult = "file1" + lineSeparator + "file2"
              )
              _ <- testMakeDirectory(projectRealPath)
              _ <- testCopyInto(originalProjectRoot / "file1", projectRealPath)
              _ <- testMakeDirectory(projectRealPath)
              _ <- testCopyInto(originalProjectRoot / "file2", projectRealPath)
            } yield Seq("all")
          )
        },
        test("instruction when onlyMutateDiff = true and git diff returns empty") {
          val inst = filesToMutateInstruction(
            onlyMutateDiff = true,
            filesToMutate = "src"
          )

          testInstruction(
            inst,
            for {
              _ <- testRunAsync(
                "git",
                Seq("rev-parse", "master"),
                Map.empty,
                gitFolder,
                mockResult = "abc123"
              )
              _ <- testRunAsync(
                "git",
                Seq("--no-pager", "diff", "--name-only", "abc123"),
                Map.empty,
                gitFolder,
                mockResult = ""
              )
            } yield Seq.empty
          )
        },
        test("instruction when onlyMutateDiff = true") {
          val inst = filesToMutateInstruction(
            onlyMutateDiff = true,
            filesToMutate = "folder"
          )

          testInstruction(
            inst,
            for {
              _ <- testRunAsync(
                "git",
                Seq("rev-parse", "master"),
                Map.empty,
                gitFolder,
                mockResult = "abc123"
              )
              _ <- testRunAsync(
                "git",
                Seq("--no-pager", "diff", "--name-only", "abc123"),
                Map.empty,
                gitFolder,
                mockResult =
                  "file1.scala" + lineSeparator + "file2.md" + lineSeparator + "folder/file3.scala"
              )

              _ <- testRunAsync(
                "git",
                Seq("ls-files", "--others", "--exclude-standard", "--cached"),
                Map.empty,
                originalProjectPath,
                mockResult =
                  "file1.scala" + lineSeparator + "file2.md" + lineSeparator +
                    "folder/file3.scala" + lineSeparator + "file4.scala"
              )
              _ <- testMakeDirectory(projectRealPath)
              _ <- testCopyInto(originalProjectRoot / "file1.scala", projectRealPath)
              _ <- testMakeDirectory(projectRealPath)
              _ <- testCopyInto(originalProjectRoot / "file2.md", projectRealPath)
              _ <- testMakeDirectory(projectRealPath / "folder")
              _ <- testCopyInto(originalProjectRoot / "folder" / "file3.scala", projectRealPath)
              _ <- testMakeDirectory(projectRealPath)
              _ <- testCopyInto(originalProjectRoot / "file4.scala", projectRealPath)

              _ <- testIsFile(pwd / "folder", mockResult = false)
            } yield Seq(
              "/tmp/test-project/folder/file3.scala"
            )
          )
        }
      ) /*,
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
            } yield testForceSuccess
          )
        }
      }*/
    )

  private def filesToMutateInstruction(
      onlyMutateDiff: Boolean,
      filesToMutate: String
  ): Instruction[Seq[String]] =
    Run.filesToMutateInstruction(
      onlyMutateDiff = onlyMutateDiff,
      filesToMutate = "filesToMutate",
      gitFolder,
      cloneProjectBaseFolder,
      projectRealPath,
      originalProjectPath,
      originalProjectRoot
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
