package fix

import java.io.{File => JFile}
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
  )

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
      val inputSource = new String(ruleTest.path.input.readAllBytes)
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
    props.outputSourceDirectories
      .map(outputFolder => File(outputFolder.toNIO) / ".." / "resources")
      .find(_.exists) match {
      case None =>
        fail(s".mutants file was expected to exist in resources folder for '$testRelPath'")
      case Some(path: File) =>
        File(
          path.toString + JFile.separator + testRelPath.toString.stripSuffix("scala") + "mutants"
        )
    }

  override def beforeAll(): Unit = {
    super.beforeAll()
    testsData.foreach {
      case TestData(ruleTest, inputPath, inputSource) =>
        def replaceMutantsInputFile(text: String): String = {
          val path =
            mutantsFileResolver(ruleTest.path.testPath).toString.replace("\\", "\\\\") // windows
          text.replace(
            "Blinky.mutantsOutputFile = ???",
            s"""Blinky.mutantsOutputFile = "$path""""
          )
        }

        val inputSourceReplaced = replaceMutantsInputFile(inputSource)
        Files.write(inputPath.toNIO, inputSourceReplaced.getBytes)
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    testsData.foreach {
      case TestData(_, inputPath, inputSource) =>
        Files.write(inputPath.toNIO, inputSource.getBytes)
    }
  }

  testsData.foreach { testData =>
    test(testData.ruleTest.path.testName) {
      evaluateTestBody(testData.ruleTest)
    }

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
