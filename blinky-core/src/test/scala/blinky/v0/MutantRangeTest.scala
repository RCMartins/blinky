package blinky.v0

import metaconfig.{Conf, Configured}
import zio.test._

object MutantRangeTest extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("MutantRange")(
      suite("contains")(
        test("return true if it contains the index") {
          assertTrue(
            MutantRange(2, 4).contains(2),
            MutantRange(2, 4).contains(3),
            MutantRange(2, 4).contains(4)
          )
        },
        test("return false if it does not contain the index") {
          assertTrue(
            !MutantRange(2, 4).contains(1),
            !MutantRange(2, 4).contains(5)
          )
        }
      ),
      suite("rangeDecoder")(
        test("return an error for input 'true'") {
          assertTrue(
            MutantRange.rangeDecoder.read(Conf.Bool(true)) ==
              Configured.typeMismatch("Number with a mutant index range", Conf.Bool(true))
          )
        }
      ),
      seqRangeDecoderSuite,
    )

  def seqRangeDecoderSuite: Spec[Any, Nothing] = {
    def readTest(conf: Conf, expected: Configured[Seq[MutantRange]]): TestResult =
      assertTrue(MutantRange.seqRangeDecoder.read(conf) == expected)

    suite("seqRangeDecoder")(
      test("return an error for input 'true'") {
        readTest(
          Conf.Bool(true),
          Configured.typeMismatch("Number with a mutant index range", Conf.Bool(true))
        )
      },
      test("return an error for input '0' (number)") {
        readTest(
          Conf.Num(0),
          Configured.typeMismatch("Number with a mutant index range", Conf.Num(0))
        )
      },
      test("return an error for input '0' (string)") {
        readTest(
          Conf.Str("0"),
          Configured.typeMismatch("Number with a mutant index range", Conf.Str("0"))
        )
      },
      test("return an error for input '1.5'") {
        readTest(
          Conf.Num(1.5),
          Configured.typeMismatch("Number with a mutant index range", Conf.Num(1.5))
        )
      },
      test("return an error for input '1-a'") {
        readTest(
          Conf.Str("1-a"),
          Configured.typeMismatch("Number with a mutant index range", Conf.Str("1-a"))
        )
      },
      test("return an error for input 'b-3'") {
        readTest(
          Conf.Str("b-3"),
          Configured.typeMismatch("Number with a mutant index range", Conf.Str("b-3"))
        )
      },
      test("return an error for input '1,3,-2,73,-10'") {
        readTest(
          Conf.Str("1,3,-2,73,-10"),
          Configured.typeMismatch("Number with a mutant index range", Conf.Str("-2"))
        )
      },
      test("return the correct MutantRange for input '4' (number)") {
        readTest(Conf.Num(4), Configured.ok(Seq(MutantRange(4, 4))))
      },
      test("return the correct MutantRange for input '4' (string)") {
        readTest(Conf.Str("4"), Configured.ok(Seq(MutantRange(4, 4))))
      },
      test("return the correct MutantRange for input '10-20'") {
        readTest(Conf.Str("10-20"), Configured.ok(Seq(MutantRange(10, 20))))
      },
      test("return the correct MutantRange for input '1,3,4,20-50,73'") {
        readTest(
          Conf.Str("1,3,4,20-50,73"),
          Configured.ok(
            Seq(
              MutantRange(1, 1),
              MutantRange(3, 3),
              MutantRange(4, 4),
              MutantRange(20, 50),
              MutantRange(73, 73)
            )
          )
        )
      },
      test("return the correct Conf.Str") {
        assertTrue(
          MutantRange.seqRangeEncoder.write(
            Seq(
              MutantRange(1, 1),
              MutantRange(3, 3),
              MutantRange(4, 4),
              MutantRange(20, 50),
              MutantRange(73, 73)
            )
          ) ==
            Conf.Str("1,3,4,20-50,73")
        )
      }
    )
  }

}
