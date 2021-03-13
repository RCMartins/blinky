package blinky.run

import ammonite.ops.{Path, RelPath}
import blinky.TestSpec
import blinky.run.TestInstruction._
import blinky.run.Utils.red
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
