package blinky.v0

import metaconfig.generic.Surface
import metaconfig.{ConfDecoder, generic}

import scala.collection.immutable.Seq

case class BlinkyConfig(
    mutantsOutputFile: String,
    filesToMutate: Seq[String],
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

  implicit val surface: Surface[BlinkyConfig] =
    generic.deriveSurface[BlinkyConfig]
  implicit val decoder: ConfDecoder[BlinkyConfig] =
    generic.deriveDecoder(default).noTypos
}
