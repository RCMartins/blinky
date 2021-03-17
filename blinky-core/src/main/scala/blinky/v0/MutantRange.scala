package blinky.v0

import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, Configured, generic}

case class MutantRange(startIndex: Int, endIndex: Int) {

  def contains(mutantIndex: Int): Boolean =
    startIndex >= mutantIndex && endIndex <= mutantIndex

}

object MutantRange {

  implicit val surface: Surface[MutantRange] =
    generic.deriveSurface[MutantRange]
  implicit val rangeDecoder: ConfDecoder[MutantRange] = {
    case Conf.Num(value) if value.toIntOption.isDefined =>
      val n = value.toInt
      Configured.ok(MutantRange(n, n))
    case Conf.Str(s"$n1-$n2") if n1.toIntOption.isDefined && n2.toIntOption.isDefined =>
      Configured.ok(MutantRange(n1.toInt, n2.toInt))
    case conf =>
      Configured.typeMismatch("Number with a mutant index range", conf)
  }

}
