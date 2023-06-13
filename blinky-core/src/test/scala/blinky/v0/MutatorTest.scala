package blinky.v0

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object MutatorTest extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("Mutator")(
      suite("Collections")(
        test("return the correct ListApply 'symbolsToMatch'") {
          assertTrue(
            mutators.Collections.ListApply.symbolsToMatch ==
              Seq(
                "scala/collection/immutable/List.",
                "scala/package.List."
              )
          )
        },
        test("return the correct SeqApply 'symbolsToMatch'") {
          assertTrue(
            mutators.Collections.SeqApply.symbolsToMatch ==
              Seq(
                "scala/collection/Seq.",
                "scala/collection/mutable/Seq.",
                "scala/collection/immutable/Seq.",
                "scala/package.Seq."
              )
          )
        },
        test("return the correct SetApply 'symbolsToMatch'") {
          assertTrue(
            mutators.Collections.SetApply.symbolsToMatch ==
              Seq(
                "scala/Predef.Set.",
                "scala/collection/mutable/Set.",
                "scala/collection/immutable/Set.",
                "scala/package.Set."
              )
          )
        },
        test("return the correct Reverse 'symbolsToMatch'") {
          assertTrue(
            mutators.Collections.ReverseSymbols ==
              Seq(
                "scala/collection/SeqLike#reverse().",
                "scala/collection/immutable/List#reverse().",
                "scala/collection/IndexedSeqOptimized#reverse().",
                "scala/collection/SeqOps#reverse().",
                "scala/collection/IndexedSeqOps#reverse().",
                "scala/collection/ArrayOps#reverse()."
              )
          )
        }
      )
    )

}
