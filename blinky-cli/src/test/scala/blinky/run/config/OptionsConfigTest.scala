package blinky.run.config

import metaconfig.{Conf, Configured}
import zio.test._

object OptionsConfigTest extends ZIOSpecDefault {

  val spec: Spec[TestEnvironment, TestFailure[Nothing]] =
    suite("OptionsConfig")(
      test("Return error when duration config is not a string") {
        assertTrue(
          OptionsConfig.durationDecoder.read(Conf.fromInt(50)) == Configured.error(
            """Type mismatch;
              |  found    : Number (value: 50)
              |  expected : Duration string""".stripMargin
          )
        )
      },
      test("Return error when duration config is an invalid duration") {
        assertTrue(
          OptionsConfig.durationDecoder.read(Conf.fromString("50 potatoes")) == Configured.error(
            """Type mismatch;
              |  found    : String (value: "50 potatoes")
              |  expected : Duration string""".stripMargin
          )
        )
      },
      test("Return error when double config is not a number") {
        assertTrue(
          OptionsConfig.doubleDecoder.read(Conf.fromBoolean(false)) == Configured.error(
            """Type mismatch;
              |  found    : Boolean (value: false)
              |  expected : Number""".stripMargin
          )
        )
      },
      test("Return error when multiRun config is not a string") {
        assertTrue(
          OptionsConfig.multiRunDecoder.read(Conf.fromInt(100)) == Configured.error(
            """Type mismatch;
              |  found    : Number (value: 100)
              |  expected : String in 'int/int' format""".stripMargin
          )
        )
      },
      test("Return error when multiRun config is invalid (index < 1)") {
        assertTrue(
          OptionsConfig.multiRunDecoder.read(Conf.fromString("0/2")) == Configured.error(
            """Type mismatch;
              |  found    : String (value: "0/2")
              |  expected : Invalid index value, should be >= 1""".stripMargin
          )
        )
      },
      test("Return error when multiRun config is invalid (index > amount)") {
        assertTrue(
          OptionsConfig.multiRunDecoder.read(Conf.fromString("2/1")) == Configured.error(
            """Type mismatch;
              |  found    : String (value: "2/1")
              |  expected : Invalid amount, should be greater or equal than index""".stripMargin
          )
        )
      },
      test("Return error when multiRun config is invalid #1") {
        assertTrue(
          OptionsConfig.multiRunDecoder.read(Conf.fromString("1-2")) == Configured.error(
            """Type mismatch;
              |  found    : String (value: "1-2")
              |  expected : Invalid value, should be a String in 'int/int' format""".stripMargin
          )
        )
      },
      test("Return error when multiRun config is invalid #2") {
        assertTrue(
          OptionsConfig.multiRunDecoder.read(Conf.fromString("1/all")) == Configured.error(
            """Type mismatch;
              |  found    : String (value: "1/all")
              |  expected : Invalid value, should be a String in 'int/int' format""".stripMargin
          )
        )
      },
      test("Return error when multiRun config is invalid #3") {
        assertTrue(
          OptionsConfig.multiRunDecoder.read(Conf.fromString("1/all/2")) == Configured.error(
            """Type mismatch;
              |  found    : String (value: "1/all/2")
              |  expected : Invalid value, should be a String in 'int/int' format""".stripMargin
          )
        )
      },
      test("Return error when multiRun config is invalid #4") {
        assertTrue(
          OptionsConfig.multiRunDecoder.read(Conf.fromString("1")) == Configured.error(
            """Type mismatch;
              |  found    : String (value: "1")
              |  expected : Invalid value, should be a String in 'int/int' format""".stripMargin
          )
        )
      }
    )

}
