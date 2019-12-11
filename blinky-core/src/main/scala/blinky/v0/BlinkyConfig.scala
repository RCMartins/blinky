package blinky.v0

import metaconfig.annotation.Description
import metaconfig.generic.Surface
import metaconfig.{ConfDecoder, generic}

case class BlinkyConfig(
    projectPath: String,
    mutatorsPath: String = "",
    enabledMutators: Mutators = Mutators.all,
    disabledMutators: Mutators = Mutators(Nil)
) {
  val activeMutators: List[Mutator] =
    (enabledMutators.mutations.toSet -- disabledMutators.mutations.toSet).toList
}

object BlinkyConfig {
  val default = BlinkyConfig("")
  implicit val surface: Surface[BlinkyConfig] =
    generic.deriveSurface[BlinkyConfig]
  implicit val decoder: ConfDecoder[BlinkyConfig] =
    generic.deriveDecoder(default)
}
