package blinky.v0

import blinky.TestSpec
import metaconfig.{Conf, Configured}

class MutatorsTest extends TestSpec {

  "Mutators.conf" should {

    "return an error when the Mutator doesn't exists" in {
      val configuredResult =
        Mutators.readerMutations.read(
          Conf.fromString("UnknownMutator")
        )

      configuredResult mustEqual Configured.error("UnknownMutator was not found!")
    }

    "return an error when the 'mutator name' is not valid type" in {
      val configuredResult =
        Mutators.readerMutations.read(
          Conf.fromBigDecimal(BigDecimal(0.5))
        )

      configuredResult mustEqual Configured.error(
        """Type mismatch;
          |  found    : Number (value: 0.5)
          |  expected : String with a Mutator name""".stripMargin
      )
    }

  }

}
