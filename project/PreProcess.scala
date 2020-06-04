import java.io.{File => JFile}
import java.util.concurrent.atomic.AtomicInteger

import better.files._

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
        val processedFile = outputFolder / "test" / outFile.name
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
