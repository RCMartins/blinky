package mutators

import metaconfig.ConfDecoder
import metaconfig.generic.Surface

case class MutateCodeConfig(
  mutationsPath: String = "",
  activeMutators: List[MutationType] = MutationType.all
)

object MutateCodeConfig {
  val default = MutateCodeConfig()
  implicit val surface: Surface[MutateCodeConfig] =
    metaconfig.generic.deriveSurface[MutateCodeConfig]
  implicit val decoder: ConfDecoder[MutateCodeConfig] =
    metaconfig.generic.deriveDecoder(default)
}
