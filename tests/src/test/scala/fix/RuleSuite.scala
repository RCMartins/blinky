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
      outputPath: AbsolutePath,
      inputSource: String,
      outputSource: String
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

//    println(rulesTest.map(_.path).mkString("\n"))

//    println(props)

    rulesTest.flatMap { ruleTest =>
      ruleTest.path.resolveOutput(props) match {
        case Right(outputPath) =>
//          println(outputPath)

//          val inputSource = new String(ruleTest.path.input.readAllBytes)
//          val outputSource = new String(outputPath.readAllBytes)
          Some(
            TestData(
              ruleTest,
              ruleTest.path.input,
              outputPath,
              "",
              ""
            )
          )
        case Left(_) =>
          None
      }
    }
  }

//  private val testTempFolder: File = File.newTemporaryDirectory().deleteOnExit()
//  private def mutantsFileResolver(testRelPath: RelativePath): File = {
//    val testOutputFileStr =
//      testRelPath
//        .toAbsolute(AbsolutePath(testTempFolder.toJava))
//        .toString
//    val outputFolder: File =
//      File(testOutputFileStr.substring(0, testOutputFileStr.lastIndexOf(".")))
//    outputFolder.createDirectories()
//    outputFolder / "mutants"
//  }

//  private def mutantsExpectedFileResolver(testRelPath: RelativePath): File =
//    props.outputSourceDirectories
//      .map(outputFolder => File(outputFolder.toNIO) / ".." / "resources")
//      .find(_.exists) match {
//      case None =>
//        fail(s".mutants file was expected to exist in resources folder for '$testRelPath'")
//      case Some(path: File) =>
//        File(
//          path.toString + JFile.separator + testRelPath.toString.stripSuffix("scala") + "mutants"
//        )
//    }

//  override def beforeAll(): Unit = {
//    super.beforeAll()
//    testsData.foreach {
//      case TestData(ruleTest, inputPath, outputPath, inputSource, outputSource) =>
//        def replacedQuestionMarks(before: String, id: Int): String = {
//          val after =
//            before.replaceFirst(
//              "\\?\\?\\?",
//              "_root_.scala.sys.env.contains(\"BLINKY_MUTATION_" + id + "\")"
//            )
//          if (after != before)
//            replacedQuestionMarks(after, id + 1)
//          else
//            after
//        }
//
//        def replaceLongLines(text: String): String = {
//          val replaced1 = "///\\s*".r.replaceAllIn(text, "")
//          "//\n".r.replaceAllIn(replaced1, "")
//        }
//
//        def replaceMutantsInputFile(text: String): String = {
//          val path =
//            mutantsFileResolver(ruleTest.path.testPath).toString.replace("\\", "\\\\") // windows
//          text.replace(
//            "Blinky.mutantsOutputFile = ???",
//            s"""Blinky.mutantsOutputFile = "$path""""
//          )
//        }
//
//        val inputSourceReplaced = replaceMutantsInputFile(inputSource)
//        Files.write(inputPath.toNIO, inputSourceReplaced.getBytes)
//
//        val outputSourceReplaced = replaceLongLines(replacedQuestionMarks(outputSource, 1))
//        Files.write(outputPath.toNIO, outputSourceReplaced.getBytes)
//    }
//  }

//  override def afterAll(): Unit = {
//    super.afterAll()
//    testsData.foreach {
//      case TestData(_, inputPath, outputPath, inputSource, outputSource) =>
//        Files.write(inputPath.toNIO, inputSource.getBytes)
//        Files.write(outputPath.toNIO, outputSource.getBytes)
//    }
//  }

  testsData.foreach { testData =>
    test(testData.ruleTest.path.testName) {
      evaluateTestBody(testData.ruleTest)
    }

//    val mutantsExpectedFile = mutantsExpectedFileResolver(testData.ruleTest.path.testPath)
//    if (mutantsExpectedFile.exists)
//      test(testData.ruleTest.path.testName + " (mutants file)") {
//        val mutantsFile: File = mutantsFileResolver(testData.ruleTest.path.testPath)
//
//        if (mutantsFile.lines == mutantsExpectedFile.lines)
//          succeed
//        else
//          fail(s"""Actual:
//                  |${mutantsFile.contentAsString}
//                  |Expected:
//                  |${mutantsExpectedFile.contentAsString}
//                  |""".stripMargin)
//      }
  }

}
