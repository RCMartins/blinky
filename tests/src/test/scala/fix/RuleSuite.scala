package fix

import java.nio.file.Files

import better.files._
import scalafix.testkit.{RuleTest, SemanticRuleSuite}

import scala.meta.io.{AbsolutePath, RelativePath}

class RuleSuite extends SemanticRuleSuite() {

  private val only: Option[String] =
//    Some("All1") // << to run only one test
    None

  private case class TestData(
      ruleTest: RuleTest,
      inputPath: AbsolutePath,
      inputSource: String
  ) {
    val hasMutationsFile: Boolean = inputSource.nonEmpty
  }

  private val testsData: Seq[TestData] = {
    val rulesTest: List[RuleTest] =
      only match {
        case Some(file) =>
          testsToRun
            .filter(_.path.testPath.toNIO.getFileName.toString.stripSuffix(".scala") == file)
        case None =>
          testsToRun
      }

    rulesTest.map { ruleTest =>
      val inputSource = {
        val source = new String(ruleTest.path.input.readAllBytes)
        if (source.contains("Blinky.mutantsOutputFile = ???")) source else ""
      }
      TestData(
        ruleTest,
        ruleTest.path.input,
        inputSource
      )
    }
  }

  private val testTempFolder: File = File.newTemporaryDirectory().deleteOnExit()
  private def mutantsFileResolver(testRelPath: RelativePath): File = {
    val testOutputFileStr =
      testRelPath
        .toAbsolute(AbsolutePath(testTempFolder.toJava))
        .toString
    val outputFolder: File =
      File(testOutputFileStr.substring(0, testOutputFileStr.lastIndexOf(".")))
    outputFolder.createDirectories()
    outputFolder / "mutants"
  }

  private def mutantsExpectedFileResolver(testRelPath: RelativePath): File =
    (File(
      props.outputSourceDirectories.head.toNIO
    ) / ".." / ".." / "classes").path.resolve(testRelPath.toString.stripSuffix("scala") + "mutants")

  override def beforeAll(): Unit = {
    super.beforeAll()
    testsData.foreach { testData =>
      if (testData.hasMutationsFile) {
        val inputSourceReplaced = {
          val path =
            mutantsFileResolver(testData.ruleTest.path.testPath).toString
              .replace("\\", "\\\\") // windows
          testData.inputSource.replace(
            "Blinky.mutantsOutputFile = ???",
            s"""Blinky.mutantsOutputFile = "$path""""
          )
        }

        Files.write(testData.inputPath.toNIO, inputSourceReplaced.getBytes)
      }
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    testsData.foreach { testData =>
      if (testData.hasMutationsFile)
        Files.write(testData.inputPath.toNIO, testData.inputSource.getBytes)
    }
  }

  testsData.foreach { testData =>
    test(testData.ruleTest.path.testName) {
      evaluateTestBody(testData.ruleTest)
    }

    if (testData.hasMutationsFile) {
      val mutantsExpectedFile = mutantsExpectedFileResolver(testData.ruleTest.path.testPath)
      if (mutantsExpectedFile.exists)
        test(testData.ruleTest.path.testName + " (mutants file)") {
          val mutantsFile: File = mutantsFileResolver(testData.ruleTest.path.testPath)

          if (mutantsFile.lines == mutantsExpectedFile.lines)
            succeed
          else
            fail(s"""Actual:
                    |${mutantsFile.contentAsString}
                    |Expected:
                    |${mutantsExpectedFile.contentAsString}
                    |""".stripMargin)
        }
    }
  }

}
