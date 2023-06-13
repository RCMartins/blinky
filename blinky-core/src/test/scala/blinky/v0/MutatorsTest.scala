package blinky.v0

import metaconfig.{Conf, Configured}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object MutatorsTest extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("Mutators")(
      suite("conf")(
        test("return an error when the Mutator doesn't exists") {
          val configuredResult =
            Mutators.readerMutations.read(
              Conf.fromString("UnknownMutator")
            )

          assertTrue(configuredResult == Configured.error("UnknownMutator was not found!"))
        },
        test("return an error when the 'mutator name' is not valid type") {
          val configuredResult =
            Mutators.readerMutations.read(
              Conf.fromBigDecimal(BigDecimal(0.5))
            )

          assertTrue(
            configuredResult == Configured.error(
              """Type mismatch;
                |  found    : Number (value: 0.5)
                |  expected : String with a Mutator name""".stripMargin
            )
          )
        }
      )
    )

}
