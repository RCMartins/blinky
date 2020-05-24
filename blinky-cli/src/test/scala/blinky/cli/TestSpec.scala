package blinky.cli

import org.scalatest.{AppendedClues, OptionValues}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait TestSpec extends AnyWordSpec with Matchers with OptionValues with AppendedClues
