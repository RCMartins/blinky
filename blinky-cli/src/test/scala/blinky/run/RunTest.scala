package blinky.run

import ammonite.ops.{Path, RelPath}
import blinky.TestSpec
import blinky.run.TestInstruction._
import blinky.run.Utils.red
import blinky.run.config.FileFilter
import zio.ExitCode
import zio.test._
import zio.test.environment.TestEnvironment

import java.io.IOException

object RunTest extends TestSpec {

  val originalProjectRoot: Path = Path(getFilePath("."))
  val originalProjectPath: Path = Path(getFilePath("some-project"))
  val projectRealPath: Path = Path(getFilePath(".")) / "some-temp-folder"

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Run")(
      suite("processFilesToMutate")(
        test("when files filtering is SingleFileOrFolder") {
          testInstruction(
            Run.processFilesToMutate(
              projectRealPath,
              FileFilter.SingleFileOrFolder(RelPath("src/main/scala/SomeFile.scala"))
            ),
            TestReturn(Right("src/main/scala/SomeFile.scala"))
          )
        },
        test("when filterFiles returns empty") {
          testInstruction(
            Run.processFilesToMutate(
              projectRealPath,
              FileFilter.FileName("FileD.scala")
            ),
            TestLsFiles(
              projectRealPath,
              Seq("src/FileA.scala", "src/FileB.scala", "src/FileC.scala"),
              TestPrintLine(
                s"--filesToMutate 'FileD.scala' does not exist.",
                TestReturn(Left(ExitCode.failure))
              )
            )
          )
        },
        test("when filterFiles returns exactly one file") {
          testInstruction(
            Run.processFilesToMutate(
              projectRealPath,
              FileFilter.FileName("FileA.scala")
            ),
            TestLsFiles(
              projectRealPath,
              Seq("src/FileA.scala", "src/FileB.scala", "src/FileC.scala"),
              TestReturn(Right("src/FileA.scala"))
            )
          )
        },
        test("when filterFiles returns multiple files") {
          testInstruction(
            Run.processFilesToMutate(
              projectRealPath,
              FileFilter.FileName("FileB.scala")
            ),
            TestLsFiles(
              projectRealPath,
              Seq("src/FileA.scala", "src/foo/FileB.scala", "src/bar/FileB.scala"),
              TestPrintLine(
                s"""--filesToMutate is ambiguous.
                   |Files ending with the same path:
                   |src/foo/FileB.scala
                   |src/bar/FileB.scala""".stripMargin,
                TestReturn(Left(ExitCode.failure))
              )
            )
          )
        }
      ),
      suite("copyFilesToTempFolder")(
        test("when both git and copy works") {
          testInstruction(
            Run.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
            TestRunAsync(
              "git",
              Seq("ls-files", "--others", "--exclude-standard", "--cached"),
              Map.empty,
              originalProjectPath,
              mockResult =
                Right(Seq("src/main/scala/SomeFile.scala").mkString(System.lineSeparator())),
              TestCopyRelativeFiles(
                Seq(RelPath("src/main/scala/SomeFile.scala")),
                originalProjectRoot,
                projectRealPath,
                Right(()),
                TestReturn(Right(()))
              )
            )
          )
        },
        test("when git works but the copy fails") {
          testInstruction(
            Run.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
            TestRunAsync(
              "git",
              Seq("ls-files", "--others", "--exclude-standard", "--cached"),
              Map.empty,
              originalProjectPath,
              mockResult =
                Right(Seq("src/main/scala/SomeFile.scala").mkString(System.lineSeparator())),
              TestCopyRelativeFiles(
                Seq(RelPath("src/main/scala/SomeFile.scala")),
                originalProjectRoot,
                projectRealPath,
                Left(new IOException("Error from copying files")),
                TestPrintLine(
                  "Error copying project files: java.io.IOException: Error from copying files",
                  TestReturn(Right(()))
                )
              )
            )
          )
        },
        test("when git fails") {
          testInstruction(
            Run.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
            TestRunAsync(
              "git",
              Seq("ls-files", "--others", "--exclude-standard", "--cached"),
              Map.empty,
              originalProjectPath,
              mockResult = Left("git command error message"),
              TestPrintLine(
                s"""${red("GIT command error:")}
                   |git command error message
                   |""".stripMargin,
                TestReturn(Left(ExitCode.failure))
              )
            )
          )
        }
      )
    )

}
