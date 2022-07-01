package blinky

import zio.test.Assertion._
import zio.test.{Assertion, ZIOSpecDefault}

trait TestSpec extends ZIOSpecDefault {

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

  def equalSome[A, P](value: A): Assertion[Any] =
    equalTo(Some(value))

}
