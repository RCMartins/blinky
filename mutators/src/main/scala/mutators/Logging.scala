package mutators

object Logging {

  def log(obj: Any): Unit = println(obj)

  def red(obj: Any): String = s"\u001B[31m" + obj + "\u001B[0m"

  def green(obj: Any): String = s"\u001B[32m" + obj + "\u001B[0m"

  def yellow(obj: Any): String = s"\u001B[33m" + obj + "\u001B[0m"

  def blue(obj: Any): String = s"\u001B[34m" + obj + "\u001B[0m"

  def magenta(obj: Any): String = s"\u001B[35m" + obj + "\u001B[0m"

  def cyan(obj: Any): String = s"\u001B[36m" + obj + "\u001B[0m"

}
