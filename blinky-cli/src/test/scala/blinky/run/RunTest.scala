package blinky.run

import blinky.TestSpec._
import blinky.run.TestInstruction._
import blinky.run.Utils.red
import blinky.run.config.FileFilter
import os.{Path, RelPath}
import zio.test._
import zio.{ExitCode, Scope}

import java.io.IOException

object RunTest extends ZIOSpecDefault {

  val originalProjectRoot: Path = Path(getFilePath("."))
  val originalProjectPath: Path = Path(getFilePath("some-project"))
  val projectRealPath: Path = Path(getFilePath(".")) / "some-temp-folder"

  def spec: Spec[TestEnvironment with Scope, Any] =
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
              Right(Seq("src/FileA.scala", "src/FileB.scala", "src/FileC.scala")),
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
              Right(Seq("src/FileA.scala", "src/FileB.scala", "src/FileC.scala")),
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
              Right(Seq("src/FileA.scala", "src/foo/FileB.scala", "src/bar/FileB.scala")),
              TestPrintLine(
                s"""--filesToMutate is ambiguous.
                   |Files ending with the same path:
                   |src/foo/FileB.scala
                   |src/bar/FileB.scala""".stripMargin,
                TestReturn(Left(ExitCode.failure))
              )
            )
          )
        },
        test("when lists files command fails") {
          testInstruction(
            Run.processFilesToMutate(
              projectRealPath,
              FileFilter.FileName("FileB.scala")
            ),
            TestLsFiles(
              projectRealPath,
              Left(new Throwable("ls-files command error message")),
              TestPrintErrorLine(
                s"""Failed to list files in $projectRealPath
                   |java.lang.Throwable: ls-files command error message
                   |""".stripMargin,
                TestReturn(Left(ExitCode.failure))
              )
            )
          )
        }
      ),
      suite("optimiseFilesToMutate")(
        test("when copy result fails") {
          testInstruction(
            Run.optimiseFilesToMutate(
              Seq.empty,
              Left(ExitCode.failure),
              projectRealPath,
              FileFilter.FileName("Any")
            ),
            TestReturn(Left(ExitCode.failure))
          )
        },
        test("when copy result succeeds and filter is FileName and there is only one file") {
          val singleFile = (projectRealPath / "FileA.scala").toString
          testInstruction(
            Run.optimiseFilesToMutate(
              Seq(singleFile),
              Right(()),
              projectRealPath,
              FileFilter.FileName("FileA.scala")
            ),
            TestIsFile(
              Path(singleFile),
              mockResult = true,
              TestReturn(Right((singleFile, Seq(singleFile))))
            )
          )
        },
        test(
          "when copy result succeeds and filter is SingleFileOrFolder and there is only one folder"
        ) {
          val singleFolder = (projectRealPath / "src").toString
          testInstruction(
            Run.optimiseFilesToMutate(
              Seq(singleFolder),
              Right(()),
              projectRealPath,
              FileFilter.SingleFileOrFolder(RelPath("src"))
            ),
            TestIsFile(
              Path(singleFolder),
              mockResult = true,
              TestReturn(Right((singleFolder, Seq(singleFolder))))
            )
          )
        }
      ),
      suite("copyFilesToTempFolder")(
        test("when both git and copy works") {
          testInstruction(
            Run.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
            TestRunResultEither(
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
            TestRunResultEither(
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
            TestRunResultEither(
              "git",
              Seq("ls-files", "--others", "--exclude-standard", "--cached"),
              Map.empty,
              originalProjectPath,
              mockResult = Left(new Throwable("git command error message")),
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
