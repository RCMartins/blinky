package blinky.run

import better.files.File
import blinky.TestSpec._
import blinky.run.TestInstruction._
import blinky.run.Utils.red
import blinky.run.config._
import blinky.run.modules.TestModules
import com.softwaremill.quicklens.ModifyPimp
import os.{Path, RelPath}
import zio._
import zio.test._

import java.io.IOException

object RunTest extends ZIOSpecDefault {

  private val originalProjectRoot: Path = Path(getFilePath("."))
  private val originalProjectPath: Path = Path(getFilePath("some-project"))
  private val cloneProjectBaseFolder: Path = Path(getFilePath(".")) / "some-temp-folder"
  private val projectRealPath: Path = cloneProjectBaseFolder / "clone-project"
  private val someException = SomeException("some exception")

  def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("Run")(
      runSuite,
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
        test("when file does not exist") {
          testInstruction(
            Run.optimiseFilesToMutate(
              Seq(),
              Right(()),
              projectRealPath,
              FileFilter.FileName("FileA.scala")
            ),
            TestPrintLine(
              s"--filesToMutate 'FileA.scala' does not exist.",
              TestReturn(Left(ExitCode.failure))
            )
          )
        },
        test(
          "when copy result succeeds and filter is SingleFileOrFolder and there is only one file"
        ) {
          val singleFile = (projectRealPath / "src" / "File.scala").toString
          testInstruction(
            Run.optimiseFilesToMutate(
              Seq(singleFile),
              Right(()),
              projectRealPath,
              FileFilter.SingleFileOrFolder(RelPath("src/File.scala"))
            ),
            TestIsFile(
              Path(singleFile),
              mockResult = true,
              TestReturn(Right((singleFile, Seq(singleFile))))
            )
          )
        },
        test(
          "when copy result succeeds and filter is SingleFileOrFolder and files don't match"
        ) {
          val file1 = (projectRealPath / "src" / "File1.scala").toString
          val file2 = (projectRealPath / "src" / "File2.scala").toString
          val file3 = (projectRealPath / "src" / "File3.scala").toString
          testInstruction(
            Run.optimiseFilesToMutate(
              Seq(file1, file2),
              Right(()),
              projectRealPath,
              FileFilter.SingleFileOrFolder(RelPath("src/File3.scala"))
            ),
            TestIsFile(
              Path(file3),
              mockResult = true,
              TestReturn(Right((file3, Seq.empty)))
            )
          )
        },
        test(
          "when copy result succeeds and filter is SingleFileOrFolder and there is only one folder"
        ) {
          val folder = (projectRealPath / "src").toString
          val file1 = (projectRealPath / "src" / "File1.scala").toString
          val file2 = (projectRealPath / "src" / "File2.scala").toString
          testInstruction(
            Run.optimiseFilesToMutate(
              Seq(file1, file2),
              Right(()),
              projectRealPath,
              FileFilter.SingleFileOrFolder(RelPath("src"))
            ),
            TestIsFile(
              Path(folder),
              mockResult = false,
              TestReturn(Right((folder, Seq(file1, file2))))
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
              mockResult = Right("src/main/scala/SomeFile.scala"),
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
              mockResult = Right("src/main/scala/SomeFile.scala"),
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

  private def runSuite: Spec[Any, Throwable] = {
    val projectFile = File(originalProjectPath.toString)
    val dummyMutationsConfigValidated: MutationsConfigValidated =
      MutationsConfigValidated(
        projectPath = projectFile,
        filesToMutate = FileFilter.SingleFileOrFolder(RelPath("src")),
        filesToExclude = "",
        mutators = SimpleBlinkyConfig.default,
        options = OptionsConfig.default,
      )

    suite("run")(
      test("error if creating temporary folder fails") {
        Run.run(dummyMutationsConfigValidated).flatMap {
          testInstruction(
            _,
            TestMakeTemporaryDirectory(
              Left(someException),
              TestPrintErrorLine(
                s"""Error creating temporary folder:
                   |blinky.TestSpec$$SomeException: some exception
                   |""".stripMargin,
                TestReturn(ExitCode.failure)
              ),
            )
          )
        }
      },
      test("error if 'git rev-parse ...' fails (with verbose=true)") {
        Run.run(dummyMutationsConfigValidated.modify(_.options.verbose).setTo(true)).flatMap {
          testInstruction(
            _,
            TestMakeTemporaryDirectory(
              Right(cloneProjectBaseFolder),
              TestPrintLine(
                s"Temporary project folder: $cloneProjectBaseFolder",
                TestRunResultEither(
                  "git",
                  Seq("rev-parse", "--show-toplevel"),
                  Map.empty,
                  originalProjectPath,
                  Left(someException),
                  TestPrintErrorLine(
                    s"""${red("GIT command error:")}
                       |blinky.TestSpec$$SomeException: some exception
                       |""".stripMargin,
                    TestReturn(ExitCode.failure)
                  ),
                ),
              ),
            )
          )
        }
      }
    ).provide(TestModules.testCliModule(projectFile))
  }

}
