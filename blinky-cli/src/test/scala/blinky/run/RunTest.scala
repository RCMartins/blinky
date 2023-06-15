package blinky.run

import better.files.File
import blinky.TestSpec._
import blinky.run.TestInstruction._
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
  private val cloneProjectTempFolder: Path = Path(getFilePath(".")) / "some-temp-folder"
  private val projectRealPath: Path = cloneProjectTempFolder / "clone-project"
  private val projectBaseFolder: Path = cloneProjectTempFolder / "some-project"
  private val someException = SomeException("some exception")
  private val projectFile = File(originalProjectPath.toString)

  def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("Run")(
      runSuite,
      suite("processFilesToMutate")(
        test("when files filtering is SingleFileOrFolder") {
          testRun(
            _.processFilesToMutate(
              projectRealPath,
              FileFilter.SingleFileOrFolder(RelPath("src/main/scala/SomeFile.scala"))
            ),
            TestReturn(Right("src/main/scala/SomeFile.scala"))
          )
        },
        test("when filterFiles returns empty") {
          testRun(
            _.processFilesToMutate(
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
          testRun(
            _.processFilesToMutate(
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
          testRun(
            _.processFilesToMutate(
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
          testRun(
            _.processFilesToMutate(
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
          testRun(
            _.optimiseFilesToMutate(
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
          testRun(
            _.optimiseFilesToMutate(
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
          testRun(
            _.optimiseFilesToMutate(
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
          testRun(
            _.optimiseFilesToMutate(
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
          testRun(
            _.optimiseFilesToMutate(
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
          testRun(
            _.optimiseFilesToMutate(
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
          testRun(
            _.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
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
          testRun(
            _.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
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
          testRun(
            _.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
            TestRunResultEither(
              "git",
              Seq("ls-files", "--others", "--exclude-standard", "--cached"),
              Map.empty,
              originalProjectPath,
              mockResult = Left(new Throwable("git command error message")),
              TestPrintLine(
                s"""${redText("GIT command error:")}
                   |git command error message
                   |""".stripMargin,
                TestReturn(Left(ExitCode.failure))
              )
            )
          )
        }
      )
    ).provideShared(
      TestModules.testCliModule(projectFile) >+>
        ZLayer.succeed(RunMutationsSBT) >+>
        PrettyDiff.live >+>
        RunMutations.live >+>
        Run.live
    )

  private def runSuite: Spec[Run, Throwable] = {
    val dummyMutationsConfigValidated: MutationsConfigValidated =
      MutationsConfigValidated(
        projectPath = projectFile,
        filesToMutate = FileFilter.SingleFileOrFolder(RelPath("src")),
        filesToExclude = "",
        mutators = SimpleBlinkyConfig.default,
        options = OptionsConfig.default,
      )

    def diffLinesTest(
        diffLines: Either[Throwable, String],
        result: TestInstruction[ExitCode]
    ): ZIO[Run, Nothing, TestResult] =
      testRun(
        _.run(dummyMutationsConfigValidated.modify(_.options.onlyMutateDiff).setTo(true)),
        TestMakeTemporaryDirectory(
          Right(cloneProjectTempFolder),
          TestRunResultEither(
            "git",
            Seq("rev-parse", "--show-toplevel"),
            Map.empty,
            originalProjectPath,
            Right(originalProjectPath.toString),
            TestMakeDirectory(
              projectBaseFolder,
              Right(()),
              TestRunResultEither(
                "git",
                Seq("rev-parse", "main"),
                Map.empty,
                originalProjectPath,
                Right("hash123456789"),
                TestRunResultEither(
                  "git",
                  Seq("--no-pager", "diff", "--name-only", "hash123456789"),
                  Map.empty,
                  originalProjectPath,
                  diffLines,
                  result,
                ),
              ),
            ),
          ),
        )
      )

    suite("run")(
      test("error if creating temporary folder fails") {
        testRun(
          _.run(dummyMutationsConfigValidated),
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
      },
      test("error if 'git rev-parse --show-toplevel' fails (with verbose=true)") {
        testRun(
          _.run(dummyMutationsConfigValidated.modify(_.options.verbose).setTo(true)),
          TestMakeTemporaryDirectory(
            Right(cloneProjectTempFolder),
            TestPrintLine(
              s"Temporary project folder: $cloneProjectTempFolder",
              TestRunResultEither(
                "git",
                Seq("rev-parse", "--show-toplevel"),
                Map.empty,
                originalProjectPath,
                Left(someException),
                TestPrintLine(
                  s"""${redText("GIT command error:")}
                     |some exception
                     |""".stripMargin,
                  TestReturn(ExitCode.failure)
                ),
              ),
            ),
          )
        )
      },
      test(
        "error if 'git rev-parse <main-branch>' fails (onlyMutateDiff=true and mainBranch=main-branch)"
      ) {
        testRun(
          _.run(
            dummyMutationsConfigValidated
              .modify(_.options.onlyMutateDiff)
              .setTo(true)
              .modify(_.options.mainBranch)
              .setTo("main-branch")
          ),
          TestMakeTemporaryDirectory(
            Right(cloneProjectTempFolder),
            TestRunResultEither(
              "git",
              Seq("rev-parse", "--show-toplevel"),
              Map.empty,
              originalProjectPath,
              Right(originalProjectPath.toString),
              TestMakeDirectory(
                projectBaseFolder,
                Right(()),
                TestRunResultEither(
                  "git",
                  Seq("rev-parse", "main-branch"),
                  Map.empty,
                  originalProjectPath,
                  Left(someException),
                  TestPrintLine(
                    s"""${redText("GIT command error:")}
                       |some exception
                       |""".stripMargin,
                    TestReturn(ExitCode.failure)
                  ),
                ),
              ),
            ),
          )
        )
      },
      test("error if 'git --no-pager diff --name-only <hash>'") {
        diffLinesTest(
          Left(someException),
          TestPrintLine(
            s"""${redText("GIT command error:")}
               |some exception
               |""".stripMargin,
            TestReturn(ExitCode.failure)
          )
        )
      },
      test("when 'git --no-pager diff --name-only <hash>' works but there are no files") {
        diffLinesTest(
          Right(""), // no files
          TestPrintLine(
            s"""${greenText(
                "0 files to mutate because no code change found due to --onlyMutateDiff flag."
              )}
               |If you want all files to be tested regardless use --onlyMutateDiff=false
               |""".stripMargin,
            TestReturn(ExitCode.success)
          )
        )
      },
      test("when 'git --no-pager diff --name-only <hash>' works but there are no scala files") {
        diffLinesTest(
          Right("SomeFile.md\nOtherFile.conf"), // no scala files
          TestPrintLine(
            s"""${greenText(
                "0 files to mutate because no code change found due to --onlyMutateDiff flag."
              )}
               |If you want all files to be tested regardless use --onlyMutateDiff=false
               |""".stripMargin,
            TestReturn(ExitCode.success)
          )
        )
      },
    )
  }

  private def testRun[A](
      actualInstruction: Run => Instruction[A],
      expectationInstruction: TestInstruction[A]
  ): ZIO[Run, Nothing, TestResult] =
    for {
      instance <- ZIO.service[Run]
    } yield testInstruction(
      actualInstruction(instance),
      expectationInstruction
    )

}
