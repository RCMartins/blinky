package blinky

object TestSpec {

  @inline
  final val inWindows: Boolean =
    System.getProperty("os.name").toLowerCase.contains("win")

  @inline
  final val removeCarriageReturns: String => String =
    if (inWindows) _.replace("\r", "") else identity

  @inline
  final def getFilePath: String => String =
    if (inWindows)
      fileName => getClass.getResource(s"/$fileName").getPath.stripPrefix("/")
    else
      fileName => getClass.getResource(s"/$fileName").getPath

  case class SomeException(message: String) extends Exception(message)

  def redText(str: String): String = s"\u001B[31m" + str + "\u001B[0m"

  def greenText(str: String): String = s"\u001B[32m" + str + "\u001B[0m"

}
