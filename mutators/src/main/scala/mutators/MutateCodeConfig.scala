package mutators

import metaconfig.annotation.Description
import metaconfig.{ConfDecoder, generic}
import metaconfig.generic.Surface

case class MutateCodeConfig(
    @Description("The project directory, required")
    projectPath: String,
    @Description("The mutation types to use, defaults to all types")
    activeMutators: List[MutationType] = MutationType.all
)

object MutateCodeConfig {
  val default = MutateCodeConfig("")
  implicit val surface: Surface[MutateCodeConfig] =
    generic.deriveSurface[MutateCodeConfig]
  implicit val decoder: ConfDecoder[MutateCodeConfig] =
    generic.deriveDecoder(default)
}
