package blinky.v0

class BlinkyConfigTest extends TestSpec {

  "BlinkyConfig.default" should {

    "return the correct default settings" in {
      BlinkyConfig.default mustEqual
        BlinkyConfig(
          mutantsOutputFile = "",
          filesToMutate = Seq.empty,
          enabledMutators = Mutators.all,
          disabledMutators = Mutators(Nil)
        )
    }

  }

}
