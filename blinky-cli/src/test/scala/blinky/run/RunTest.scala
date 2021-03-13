package blinky.run

import ammonite.ops.{Path, pwd}
import blinky.TestSpec
import blinky.run.TestInstruction._
import zio.test._
import zio.test.environment.TestEnvironment

object RunTest extends TestSpec {

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Run")(
      suite("copyFilesToTempFolder")(
        test("when git and copy works correctly") {
          val originalProjectRoot: Path = ???
          val originalProjectPath: Path = ???
          val projectRealPath: Path = ???

          testInstruction(
            Run.copyFilesToTempFolder(originalProjectRoot, originalProjectPath, projectRealPath),
            TestRunAsync(
              "git",
              Seq("ls-files", "--others", "--exclude-standard", "--cached"),
              Map.empty,
              originalProjectPath,
              mockResult = Seq("src/main/scala/SomeFile.scala").mkString(System.lineSeparator()),
              TestCopyRelativeFiles(
                Seq(???),
                originalProjectRoot,
                projectRealPath,
                Right(()),
                TestReturn(())
              )
            )
          )
        }
      )
    )

}
