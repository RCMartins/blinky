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
    val List(startLineMinus, contextLinesMinus, startLinePlus, contextLinesPlus): List[Int] =
      header.substring(4, header.lastIndexOf("@@")).split("[,+-]").toList.map(_.trim.toInt)

    val endLineMinus: Int = startLineMinus + contextLinesMinus - 1
    val endLinePlus: Int = startLinePlus + contextLinesPlus - 1

    val lineNumbersLength: Int = 3 + Math.log10(Math.max(endLineMinus, endLinePlus)).toInt
    val lineNumbersLengthArgInt: String = "%" + lineNumbersLength + "d"
    val lineNumbersLengthArgEmpty: String = " " * lineNumbersLength

    def addLineNum(
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

    def addLineNumLoop(
        lines: List[String],
        numMinusOpt: Option[Int],
        numPlusOpt: Option[Int],
        afterChanges: Boolean
    ): List[String] = {
      lines match {
        case Nil =>
          Nil
        case line :: otherLines if line.startsWith(" ") =>
          addLineNum(line, numMinusOpt, numPlusOpt.filter(_ => afterChanges)) ::
            addLineNumLoop(otherLines, numMinusOpt.map(_ + 1), numPlusOpt.map(_ + 1), afterChanges)
        case line :: otherLines if line.startsWith("-") =>
          addLineNum(line, numMinusOpt, None) ::
            addLineNumLoop(otherLines, numMinusOpt.map(_ + 1), numPlusOpt, afterChanges = true)
        case line :: otherLines if line.startsWith("+") =>
          addLineNum(line, None, numPlusOpt) ::
            addLineNumLoop(otherLines, numMinusOpt, numPlusOpt.map(_ + 1), afterChanges = true)
      }
    }

    val gitDiffLineNumbers =
      addLineNumLoop(
        lines = diffLines,
        numMinusOpt = Some(startLineMinus),
        numPlusOpt = Some(startLinePlus),
        afterChanges = false
      )

    s"""${stripPathPrefix(fileName, projectPath)}
       |${if (color) cyan(header) else header}
       |${gitDiffLineNumbers.mkString("\n")}""".stripMargin
  }

  def escapeString(str: String): String = {
    str.replace("\"", "\\\"")
  }

}
