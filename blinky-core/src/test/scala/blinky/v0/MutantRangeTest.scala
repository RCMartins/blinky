package blinky.v0

import blinky.TestSpec
import metaconfig.{Conf, Configured}

class MutantRangeTest extends TestSpec {

  "MutantRange.contains" should {

    "return true if it contains the index" in {
      MutantRange(2, 4).contains(2) mustEqual true
      MutantRange(2, 4).contains(3) mustEqual true
      MutantRange(2, 4).contains(4) mustEqual true
    }

    "return false if it does not contain the index" in {
      MutantRange(2, 4).contains(1) mustEqual false
      MutantRange(2, 4).contains(5) mustEqual false
    }

  }

  "MutantRange.rangeDecoder" should {

    "return an error for input 'true'" in {
      MutantRange.rangeDecoder.read(Conf.Bool(true)) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Bool(true))
    }

  }

  "MutantRange.seqRangeDecoder" should {

    "return an error for input 'true'" in {
      readTest(Conf.Bool(true)) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Bool(true))
    }

    "return an error for input '0'" in {
      readTest(Conf.Num(0)) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Num(0))

      readTest(Conf.Str("0")) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Str("0"))
    }

    "return an error for input '1.5'" in {
      readTest(Conf.Num(1.5)) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Num(1.5))
    }

    "return an error for input '1-a'" in {
      readTest(Conf.Str("1-a")) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Str("1-a"))
    }

    "return an error for input 'b-3'" in {
      readTest(Conf.Str("b-3")) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Str("b-3"))
    }

    "return an error for input '1,3,-2,73,-10'" in {
      readTest(Conf.Str("1,3,-2,73,-10")) mustEqual
        Configured.typeMismatch("Number with a mutant index range", Conf.Str("-2"))
    }

    "return the correct MutantRange for input '4'" in {
      readTest(Conf.Num(4)) mustEqual Configured.ok(Seq(MutantRange(4, 4)))
      readTest(Conf.Str("4")) mustEqual Configured.ok(Seq(MutantRange(4, 4)))
    }

    "return the correct MutantRange for input '10-20'" in {
      readTest(Conf.Str("10-20")) mustEqual Configured.ok(Seq(MutantRange(10, 20)))
    }

    "return the correct MutantRange for input '1,3,4,20-50,73'" in {
      readTest(Conf.Str("1,3,4,20-50,73")) mustEqual
        Configured.ok(
          Seq(
            MutantRange(1, 1),
            MutantRange(3, 3),
            MutantRange(4, 4),
            MutantRange(20, 50),
            MutantRange(73, 73)
          )
        )
    }

    def readTest(conf: Conf): Configured[Seq[MutantRange]] =
      MutantRange.seqRangeDecoder.read(conf)

  }

  "MutantRange.seqRangeEncoder" should {

    "return the correct Conf.Str" in {
      MutantRange.seqRangeEncoder.write(
        Seq(
          MutantRange(1, 1),
          MutantRange(3, 3),
          MutantRange(4, 4),
          MutantRange(20, 50),
          MutantRange(73, 73)
        )
      ) mustEqual Conf.Str("1,3,4,20-50,73")
    }

  }

}
