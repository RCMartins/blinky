package blinky.v0

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object BlinkyConfigTest extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("BlinkyConfig")(
      suite("default")(
        test("return the correct default settings") {
          assertTrue(
            BlinkyConfig.default ==
              BlinkyConfig(
                mutantsOutputFile = "",
                filesToMutate = Seq.empty,
                Seq(MutantRange(1, Int.MaxValue)),
                enabledMutators = Mutators.all,
                disabledMutators = Mutators(Nil)
              )
          )
        }
      )
    )

}
