package blinky.run

import zio.ZLayer

trait PrettyDiff {

  def prettyDiff(
      diffLinesStr: String,
      fileName: String,
      projectPath: String,
      color: Boolean
  ): String

}

object PrettyDiff {

  def live: ZLayer[Any, Nothing, PrettyDiff] =
    ZLayer.succeed(PrettyDiffLive)

  private object PrettyDiffLive extends PrettyDiff {

    def prettyDiff(
        diffLinesStr: String,
        fileName: String,
        projectPath: String,
        color: Boolean
    ): String = {
      val header :: diffLines = diffLinesStr.split("\n").toList
      header.substring(4, header.lastIndexOf("@@")).split("[,+-]").toList.map(_.trim.toInt) match {
        case List(startLineMinus, contextLinesMinus, startLinePlus, contextLinesPlus) =>
          val endLineMinus: Int = startLineMinus + contextLinesMinus - 1
          val endLinePlus: Int = startLinePlus + contextLinesPlus - 1

          val lineNumbersLength: Int =
            3 + Math.log10(Math.max(endLineMinus.toDouble, endLinePlus.toDouble)).toInt
          val lineNumbersLengthArgInt: String = "%" + lineNumbersLength + "d"
          val lineNumbersLengthArgEmpty: String = " " * lineNumbersLength

          def addLineNum(
              line: String,
              numberMinusOpt: Option[Int],
              numberPlusOpt: Option[Int]
          ): String = {
            val lineWithNumbers =
              (numberMinusOpt, numberPlusOpt) match {
                case (Some(numberMinus), Some(numberPlus)) =>
                  (lineNumbersLengthArgInt + lineNumbersLengthArgInt + "   %s")
                    .format(numberMinus, numberPlus, line)
                case (Some(numberMinus), None) =>
                  (lineNumbersLengthArgInt + lineNumbersLengthArgEmpty + "   %s")
                    .format(numberMinus, line)
                case (None, Some(numberPlus)) =>
                  (lineNumbersLengthArgEmpty + lineNumbersLengthArgInt + "   %s")
                    .format(numberPlus, line)
                case (None, None) =>
                  ??? // impossible
              }

            if (color && line.startsWith("-"))
              Utils.red(lineWithNumbers)
            else if (color && line.startsWith("+"))
              Utils.green(lineWithNumbers)
            else
              lineWithNumbers
          }

          def addLineNumLoop(
              lines: List[String],
              numMinusOpt: Option[Int],
              numPlusOpt: Option[Int],
              afterChanges: Boolean
          ): List[String] =
            lines match {
              case Nil =>
                Nil
              case line :: otherLines if line.startsWith(" ") =>
                addLineNum(line, numMinusOpt, numPlusOpt.filter(_ => afterChanges)) ::
                  addLineNumLoop(
                    otherLines,
                    numMinusOpt.map(_ + 1),
                    numPlusOpt.map(_ + 1),
                    afterChanges
                  )
              case line :: otherLines if line.startsWith("-") =>
                addLineNum(line, numMinusOpt, None) ::
                  addLineNumLoop(
                    otherLines,
                    numMinusOpt.map(_ + 1),
                    numPlusOpt,
                    afterChanges = true
                  )
              case line :: otherLines =>
                addLineNum(line, None, numPlusOpt) ::
                  addLineNumLoop(
                    otherLines,
                    numMinusOpt,
                    numPlusOpt.map(_ + 1),
                    afterChanges = true
                  )
            }

          val gitDiffLineNumbers =
            addLineNumLoop(
              lines = diffLines,
              numMinusOpt = Some(startLineMinus),
              numPlusOpt = Some(startLinePlus),
              afterChanges = false
            )

          s"""${stripPathPrefix(fileName, projectPath)}
             |${if (color) Utils.cyan(header) else header}
             |${gitDiffLineNumbers.mkString("\n")}""".stripMargin
        case _ =>
          ""
      }
    }

    private def stripPathPrefix(string: String, pathPrefix: String): String = {
      val pos = string.indexOf(pathPrefix)
      if (pos == -1) string else string.substring(pos + pathPrefix.length)
    }

  }

}
