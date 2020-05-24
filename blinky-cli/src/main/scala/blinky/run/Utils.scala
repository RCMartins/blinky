package blinky.run

object Utils {

  def red(str: String): String = s"\u001B[31m" + str + "\u001B[0m"

  def green(str: String): String = s"\u001B[32m" + str + "\u001B[0m"

  def cyan(str: String): String = s"\u001B[36m" + str + "\u001B[0m"

  def stripPathPrefix(string: String, pathPrefix: String): String = {
    val pos = string.indexOf(pathPrefix)
    if (pos == -1) string else string.substring(pos + pathPrefix.length)
  }

  def prettyDiff(
      diffLinesStr: String,
      fileName: String,
      projectPath: String,
      color: Boolean
  ): String = {
    val header :: diffLines = diffLinesStr.split("\n").toList
    val startLineMinus: Int = header.substring(4, header.indexOf(",")).toInt
    val totalLines: Int = diffLines.size
    val minusLines: Int = diffLines.count(_.startsWith("-"))
    val plusLines: Int = diffLines.count(_.startsWith("+"))

    val startLinePlus: Int = startLineMinus + diffLines.takeWhile(!_.startsWith("-")).length
    val endLineMinus: Int = startLineMinus + totalLines - plusLines - 1
    val endLinePlus: Int = startLineMinus + totalLines - minusLines - 1

    val lineNumbersLength = 3 + Math.log10(Math.max(endLineMinus, endLinePlus)).toInt
    val lineNumbersLengthArgInt = "%" + lineNumbersLength + "d"
    val lineNumbersLengthArgEmpty = " " * lineNumbersLength

    def addLineNumbers(
        line: String,
        numberMinusOpt: Option[Int],
        numberPlusOpt: Option[Int]
    ): String = {
      val lineUpdated =
        if (color && line.startsWith("-"))
          red(line)
        else if (color && line.startsWith("+"))
          green(line)
        else
          line

      (numberMinusOpt, numberPlusOpt) match {
        case (Some(numberMinus), Some(numberPlus)) =>
          (lineNumbersLengthArgInt + lineNumbersLengthArgInt + "   %s")
            .format(numberMinus, numberPlus, lineUpdated)
        case (Some(numberMinus), None) =>
          (lineNumbersLengthArgInt + lineNumbersLengthArgEmpty + "   %s")
            .format(numberMinus, lineUpdated)
        case (None, Some(numberPlus)) =>
          (lineNumbersLengthArgEmpty + lineNumbersLengthArgInt + "   %s")
            .format(numberPlus, lineUpdated)
        case (None, None) =>
          ??? // impossible
      }
    }

    def addLineNumbersLoop(
        lines: List[String],
        numMinusOpt: Option[Int],
        numPlusOpt: Option[Int],
        mode: Int
    ): List[String] = {
      lines match {
        case Nil =>
          Nil
        case line :: otherLines if mode == 0 && line.startsWith(" ") =>
          addLineNumbers(line, numMinusOpt, None) ::
            addLineNumbersLoop(otherLines, numMinusOpt.map(_ + 1), numPlusOpt, mode)
        case line :: otherLines if mode == 0 && line.startsWith("-") =>
          addLineNumbers(line, numMinusOpt, None) ::
            addLineNumbersLoop(otherLines, numMinusOpt.map(_ + 1), numPlusOpt, mode)
        case line :: _ if mode == 0 && line.startsWith("+") =>
          addLineNumbersLoop(lines, numMinusOpt, numPlusOpt, mode = 1)
        case line :: otherLines if mode == 1 && line.startsWith("+") =>
          addLineNumbers(line, None, numPlusOpt) ::
            addLineNumbersLoop(otherLines, numMinusOpt, numPlusOpt.map(_ + 1), mode)
        case line :: otherLines if mode == 1 && line.startsWith(" ") =>
          addLineNumbers(line, numMinusOpt, numPlusOpt) ::
            addLineNumbersLoop(otherLines, numMinusOpt.map(_ + 1), numPlusOpt.map(_ + 1), mode)
      }
    }

    val gitDiffLineNumbers =
      addLineNumbersLoop(
        lines = diffLines,
        numMinusOpt = Some(startLineMinus),
        numPlusOpt = Some(startLinePlus),
        mode = 0
      )

    s"""${stripPathPrefix(fileName, projectPath)}
       |${if (color) cyan(header) else header}
       |${gitDiffLineNumbers.mkString("\n")}""".stripMargin
  }

  def escapeString(str: String): String = {
    str.replace("\"", "\\\"")
  }

}
