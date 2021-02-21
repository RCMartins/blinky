package blinky

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{AppendedClues, OptionValues}

trait TestSpec extends AnyWordSpec with Matchers with OptionValues with AppendedClues
