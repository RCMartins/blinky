import java.io.{File => JFile}

import better.files._

import scala.annotation.tailrec

object PreProcess {

  def preProcessOutputFiles(
      jInputFile: JFile,
      jOutputFolder: JFile
  ): Seq[JFile] = {
    val file = File(jInputFile.toPath)
    val outputFolder = File(jOutputFolder.toPath)
    file.listRecursively
      .filter(_.isRegularFile)
      .map { outFile =>
        val outputFile = outputFolder / "test" / outFile.name
        println(outputFile)
        outputFile.parent.createDirectories()
        preProcess(outFile, outputFile)
        outputFile
      }
      .toSeq
      .map(_.toJava)
  }

  private def preProcess(input: File, output: File): Unit = {
    @tailrec //TODO remove this !!!
    def replacedQuestionMarks(before: String, id: Int): String = {
      val after =
        before.replaceFirst(
          "\\?\\?\\?",
          "_root_.scala.sys.env.contains(\"BLINKY_MUTATION_" + id + "\")"
        )
      if (after != before)
        replacedQuestionMarks(after, id + 1)
      else
        after
    }

    def replaceLongLines(text: String): String = {
      val replaced1 = "///\\s*".r.replaceAllIn(text, "")
      "//\n".r.replaceAllIn(replaced1, "")
    }

    val outputFileContent = input.contentAsString
    val outputSourceReplaced = replaceLongLines(replacedQuestionMarks(outputFileContent, 1))
    output.writeText(outputSourceReplaced)
  }

}
