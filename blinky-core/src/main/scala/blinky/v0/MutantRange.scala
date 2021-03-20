package blinky.v0

import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, ConfEncoder, Configured, generic}

import scala.util.Try
import scala.util.matching.Regex

case class MutantRange(startIndex: Int, endIndex: Int) {

  def contains(mutantIndex: Int): Boolean =
    startIndex >= mutantIndex && endIndex <= mutantIndex

}

object MutantRange {

  private def isValidRangeInt(decimal: BigDecimal): Boolean =
    Try(decimal.toIntExact).toOption.exists(_ >= 1)

  private def isValidRangeInt(str: String): Boolean =
    Try(str.toInt).toOption.exists(_ >= 1)

  private val RangeRegex: Regex = "(\\d+)-(\\d+)".r

  implicit val surface: Surface[MutantRange] =
    generic.deriveSurface[MutantRange]

  implicit val rangeDecoder: ConfDecoder[MutantRange] = {
    case Conf.Num(value) if isValidRangeInt(value) =>
      val n = value.toInt
      Configured.ok(MutantRange(n, n))
    case Conf.Str(RangeRegex(n1, n2)) if isValidRangeInt(n1) && isValidRangeInt(n2) =>
      Configured.ok(MutantRange(n1.toInt, n2.toInt))
    case conf =>
      Configured.typeMismatch("Number with a mutant index range", conf)
  }

  implicit val seqRangeDecoder: ConfDecoder[Seq[MutantRange]] = {
    case Conf.Str(str) =>
      val configuredSeq =
        str.split(",").toList.map(str => MutantRange.rangeDecoder.read(Conf.Str(str)))
      configuredSeq
        .collectFirst { case configured: Configured.NotOk => configured }
        .getOrElse(Configured.ok(configuredSeq.map(_.get)))
    case conf =>
      Configured.typeMismatch("Number with a mutant index range", conf)
  }
  implicit val seqRangeEncoder: ConfEncoder[Seq[MutantRange]] =
    mutantRanges =>
      Conf.Str(mutantRanges.map { case MutantRange(n1, n2) => s"$n1-$n2" }.mkString(","))

}
