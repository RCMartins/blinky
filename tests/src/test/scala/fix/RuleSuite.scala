package fix

import java.nio.file.Files

import scalafix.testkit.SemanticRuleSuite

import scala.meta.io.AbsolutePath

class RuleSuite extends SemanticRuleSuite() {

  val outputFiles: Map[AbsolutePath, String] = testsToRun.flatMap { test =>
    test.path.resolveOutput(props) match {
      case Right(path) =>
        val source = new String(path.readAllBytes)
        Some(path -> source)
      case Left(_) =>
        None
    }
  }.toMap

  override def beforeAll(): Unit = {
    super.beforeAll()
    outputFiles.foreach {
      case (path, source) =>
        def replacedQuestionMarks(before: String, id: Int): String = {
          val after = before.replaceFirst("\\?\\?\\?", "sys.props.contains(\"SCALA_MUTATION_" + id + "\")")
          if (after != before)
            replacedQuestionMarks(after, id + 1)
          else
            after
        }

        def replaceLongLines(text: String): String = {
          val replaced1 = "///\\s*".r.replaceAllIn(text, "")
          "//\n".r.replaceAllIn(replaced1, "")
        }

        val sourceReplaced = replaceLongLines(replacedQuestionMarks(source, 1))
        Files.write(path.toNIO, sourceReplaced.getBytes)
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    outputFiles.foreach {
      case (path, source) =>
        Files.write(path.toNIO, source.getBytes)
    }
  }

  runAllTests()
}
