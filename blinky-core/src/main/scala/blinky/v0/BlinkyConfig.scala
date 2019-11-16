package blinky.v0

import metaconfig.annotation.Description
import metaconfig.generic.Surface
import metaconfig.{ConfDecoder, generic}

case class BlinkyConfig(
    @Description("The project directory, required")
    projectPath: String,
    @Description("The directory to store the mutators.json file, defaults to the project directory")
    mutatorsPath: String = "",
    @Description("The mutator types to use, defaults to all types")
    enabledMutators: Mutators = Mutators.all,
    @Description("The mutator types to disable from the enabledMutators list, defaults to empty")
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
