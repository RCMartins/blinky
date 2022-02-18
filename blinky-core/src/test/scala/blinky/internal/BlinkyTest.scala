package blinky.internal

import blinky.TestSpec
import blinky.v0.{BlinkyConfig, MutantRange, Mutators}

import scala.meta.Lit

class BlinkyTest extends TestSpec {

  "calculateGitDiff" should {

    "return empty diff if no output file is defined" in {
      val config =
        BlinkyConfig(
          mutantsOutputFile = "",
          filesToMutate = Seq.empty,
          Seq(MutantRange(1, Int.MaxValue)),
          enabledMutators = Mutators.all,
          disabledMutators = Mutators(Nil)
        )

      val original = Lit.Boolean(true)
      new Blinky(config).calculateGitDiff(original, "false") mustEqual ""
    }

  }

}
