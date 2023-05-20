import java.io.{File => JFile}
import java.util.concurrent.atomic.AtomicInteger

import better.files._

object PreProcess {

  def preProcessOutputFiles(
      jBaseInputFolder: JFile,
      jInputFile: JFile,
      jOutputFolder: JFile
  ): Seq[JFile] = {
    val baseInputFolder: File = File(jBaseInputFolder.toPath)
    val inputFile: File = File(jInputFile.toPath)
    val outputFolder: File = File(jOutputFolder.toPath)
    inputFile.listRecursively
      .filter(_.isRegularFile)
      .map { outFile =>
        val processedFile = outputFolder / baseInputFolder.relativize(outFile).toString
        processedFile.parent.createDirectories()
        preProcess(outFile, processedFile)
        processedFile
      }
      .toSeq
      .map(_.toJava)
  }

  private def preProcess(input: File, output: File): Unit = {
    def replacedQuestionMarks(str: String): String = {
      val id: AtomicInteger = new AtomicInteger(1)

      "\\?\\?\\?".r
        .replaceAllIn(
          str,
          _ => "_root_.scala.sys.env.contains(\"BLINKY_MUTATION_" + id.getAndIncrement() + "\")"
        )
    }

    def replaceLongLines(text: String): String = {
      val replaced1 = "///\\s*".r.replaceAllIn(text, "")
      "//\n".r.replaceAllIn(replaced1, "")
    }

    val outputFileContent = input.contentAsString
    val outputSourceReplaced = replaceLongLines(replacedQuestionMarks(outputFileContent))
    output.writeText(outputSourceReplaced)
  }

}
