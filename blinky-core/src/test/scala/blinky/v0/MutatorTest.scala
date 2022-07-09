package blinky.v0

import blinky.TestSpec

class MutatorTest extends TestSpec {

  "Collections" should {

    "return the correct ListApply 'symbolsToMatch'" in {
      mutators.Collections.ListApply.symbolsToMatch mustEqual
        Seq(
          "scala/collection/immutable/List.",
          "scala/package.List."
        )
    }

    "return the correct SeqApply 'symbolsToMatch'" in {
      mutators.Collections.SeqApply.symbolsToMatch mustEqual
        Seq(
          "scala/collection/Seq.",
          "scala/collection/mutable/Seq.",
          "scala/collection/immutable/Seq.",
          "scala/package.Seq."
        )
    }

    "return the correct SetApply 'symbolsToMatch'" in {
      mutators.Collections.SetApply.symbolsToMatch mustEqual
        Seq(
          "scala/Predef.Set.",
          "scala/collection/mutable/Set.",
          "scala/collection/immutable/Set.",
          "scala/package.Set."
        )
    }

    "return the correct Reverse 'symbolsToMatch'" in {
      mutators.Collections.ReverseSymbols mustEqual
        Seq(
          "scala/collection/SeqLike#reverse().",
          "scala/collection/immutable/List#reverse().",
          "scala/collection/IndexedSeqOptimized#reverse().",
          "scala/collection/SeqOps#reverse().",
          "scala/collection/IndexedSeqOps#reverse().",
          "scala/collection/ArrayOps#reverse()."
        )
    }

  }

}
