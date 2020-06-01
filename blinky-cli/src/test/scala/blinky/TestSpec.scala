package blinky

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{AppendedClues, OptionValues}

trait TestSpec extends AnyWordSpec with Matchers with OptionValues with AppendedClues {

  final val inWindows: Boolean =
    System.getProperty("os.name").toLowerCase.contains("win")

  final val removeCarriageReturns: String => String =
    if (inWindows) _.replace("\r", "") else identity

}
