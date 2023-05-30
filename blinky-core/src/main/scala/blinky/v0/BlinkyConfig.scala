package blinky.v0

import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, Configured, generic}

import scala.collection.immutable.Seq

case class BlinkyConfig(
    mutantsOutputFile: String,
    filesToMutate: Seq[(String, Seq[Range])],
    specificMutants: Seq[MutantRange],
    enabledMutators: Mutators,
    disabledMutators: Mutators
) {
  val activeMutators: List[Mutator] =
    (enabledMutators.mutations.toSet -- disabledMutators.mutations.toSet).toList
}

object BlinkyConfig {
  val default: BlinkyConfig = BlinkyConfig(
    mutantsOutputFile = "",
    filesToMutate = Seq.empty,
    specificMutants = Seq(MutantRange(1, Int.MaxValue)),
    enabledMutators = Mutators.all,
    disabledMutators = Mutators(Nil)
  )

  implicit val rangeDecoder: ConfDecoder[Range] =
    ConfigUtils.rangeDecoder("Line number or range")

  implicit val filesToMutateDecoder: ConfDecoder[(String, Seq[Range])] = {
    case Conf.Str(pathWithLines) if pathWithLines.contains(",") =>
      val (path, linesStr) = pathWithLines.span(_ != ',')
      val (invalid, valid) =
        linesStr
          .split(",")
          .toSeq
          .map(_.trim)
          .filter(_.nonEmpty)
          .map(str => rangeDecoder.read(Conf.Str(str)).toEither)
          .partitionMap(identity)

      invalid.headOption
        .map(Configured.notOk)
        .getOrElse(Configured.ok((path, valid)))
    case Conf.Str(path) =>
      Configured.ok((path, Seq.empty))
    case conf =>
      Configured.typeMismatch("Path with optional line numbers", conf)
  }

  implicit val surface: Surface[BlinkyConfig] =
    generic.deriveSurface[BlinkyConfig]
  implicit val decoder: ConfDecoder[BlinkyConfig] =
    generic.deriveDecoder(default).noTypos
}
