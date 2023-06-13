package blinky.internal

import blinky.v0.{BlinkyConfig, MutantRange, Mutators}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import scala.meta.Lit

object BlinkyTest extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("Blinky")(
      suite("calculateGitDiff")(
        test("return empty diff if no output file is defined") {
          val config =
            BlinkyConfig(
              mutantsOutputFile = "",
              filesToMutate = Seq.empty,
              Seq(MutantRange(1, Int.MaxValue)),
              enabledMutators = Mutators.all,
              disabledMutators = Mutators(Nil)
            )

          val original = Lit.Boolean(true)
          assertTrue(new Blinky(config).calculateGitDiff(original, "false") == "")
        }
      )
    )

}
