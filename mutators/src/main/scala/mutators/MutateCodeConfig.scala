package mutators

import metaconfig.annotation.Description
import metaconfig.{ConfDecoder, generic}
import metaconfig.generic.Surface

case class MutateCodeConfig(
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

object MutateCodeConfig {
  val default = MutateCodeConfig("")
  implicit val surface: Surface[MutateCodeConfig] =
    generic.deriveSurface[MutateCodeConfig]
  implicit val decoder: ConfDecoder[MutateCodeConfig] =
    generic.deriveDecoder(default)
}
