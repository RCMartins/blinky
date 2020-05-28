package blinky.v0

import metaconfig.generic.Surface
import metaconfig.{ConfDecoder, generic}

case class BlinkyConfig(
    mutantsOutputFile: String,
    filesToMutate: Seq[String],
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
    enabledMutators = Mutators.all,
    disabledMutators = Mutators(Nil)
  )

  implicit val surface: Surface[BlinkyConfig] =
    generic.deriveSurface[BlinkyConfig]
  implicit val decoder: ConfDecoder[BlinkyConfig] =
    generic.deriveDecoder(default).noTypos
}
