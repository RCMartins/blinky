package blinky.v0

import blinky.TestSpec

class BlinkyConfigTest extends TestSpec {

  "BlinkyConfig.default" should {

    "return the correct default settings" in {
      BlinkyConfig.default mustEqual
        BlinkyConfig(
          mutantsOutputFile = "",
          filesToMutate = Seq.empty,
          Seq(MutantRange(1, Int.MaxValue)),
          enabledMutators = Mutators.all,
          disabledMutators = Mutators(Nil)
        )
    }

  }

}
