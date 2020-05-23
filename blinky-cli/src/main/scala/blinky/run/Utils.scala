package blinky.run

object Utils {

  def red(str: String): String = s"\u001B[31m" + str + "\u001B[0m"

  def green(str: String): String = s"\u001B[32m" + str + "\u001B[0m"

  def prettyDiff(diffLines: List[String], projectPath: String): String = {
    val MinusRegex = "(^\\s*\\d+: -.*)".r
    val PlusRegex = "(^\\s*\\d+: +.*)".r
    diffLines
      .map {
        case MinusRegex(line) => red(line)
        case PlusRegex(line)  => green(line)
        case line             => sprintPathPrefix(line, projectPath)
      }
      .mkString("\n")
  }

  def escapeString(str: String): String = {
    str.replace("\"", "\\\"")
  }

  def sprintPathPrefix(string: String, pathPrefix: String): String = {
    val pos = string.indexOf(pathPrefix)
    if (pos == -1) string else string.substring(pos + pathPrefix.length)
  }

}
