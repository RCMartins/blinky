package blinky.run

object Utils {

  def red(str: String): String = Console.RED + str + Console.RESET

  def green(str: String): String = Console.GREEN + str + Console.RESET

  def cyan(str: String): String = Console.CYAN + str + Console.RESET

  def escapeString(str: String): String =
    str.replace("\"", "\\\"")

}
