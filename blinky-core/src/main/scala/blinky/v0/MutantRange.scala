package blinky.v0

import metaconfig.{Conf, ConfDecoder, ConfEncoder, Configured}

case class MutantRange(startIndex: Int, endIndex: Int) {

  def contains(mutantIndex: Int): Boolean =
    startIndex <= mutantIndex && endIndex >= mutantIndex

}

object MutantRange {

  implicit val rangeDecoder: ConfDecoder[MutantRange] =
    ConfigUtils
      .rangeDecoder("Number with a mutant index range")
      .map(range => MutantRange(range.start, range.end))

  implicit val seqRangeDecoder: ConfDecoder[Seq[MutantRange]] = {
    case num @ Conf.Num(_) =>
      MutantRange.rangeDecoder.read(num).map(mutantRange => Seq(mutantRange))
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
      Conf.Str(
        mutantRanges
          .map {
            case MutantRange(n1, n2) if n1 == n2 => s"$n1"
            case MutantRange(n1, n2)             => s"$n1-$n2"
          }
          .mkString(",")
      )

}
