package blinky.v0

import metaconfig.{Conf, ConfDecoder, Configured}

import scala.util.Try
import scala.util.matching.Regex

object ConfigUtils {

  private def isValidRangeInt(decimal: BigDecimal): Boolean =
    Try(decimal.toIntExact).toOption.exists(_ >= 1)

  private def isValidRangeInt(str: String): Boolean =
    str.toIntOption.exists(_ >= 1)

  private val RangeRegex: Regex = "(\\d+)-(\\d+)".r

  def rangeDecoder(errorMessage: String): ConfDecoder[Range] = {
    case Conf.Str(RangeRegex(n1, n2)) if isValidRangeInt(n1) && isValidRangeInt(n2) =>
      Configured.ok(Range.inclusive(n1.toInt, n2.toInt))
    case Conf.Str(str) if isValidRangeInt(str) =>
      val n = str.toInt
      Configured.ok(Range.inclusive(n, n))
    case Conf.Num(value) if isValidRangeInt(value) =>
      val n = value.toInt
      Configured.ok(Range.inclusive(n, n))
    case conf =>
      Configured.typeMismatch(errorMessage, conf)
  }

}
