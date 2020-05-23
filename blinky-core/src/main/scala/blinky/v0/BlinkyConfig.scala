package blinky.v0

import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, ConfEncoder, generic}

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

  implicit val mutatorEncoder: ConfEncoder[Mutator] =
    (value: Mutator) => Conf.Str(value.name)
  implicit val mutatorsEncoder: ConfEncoder[Mutators] =
    (value: Mutators) => ConfEncoder[List[Mutator]].write(value.mutations)
  implicit val surface: Surface[BlinkyConfig] =
    generic.deriveSurface[BlinkyConfig]
  implicit val decoder: ConfDecoder[BlinkyConfig] =
    generic.deriveDecoder(default).noTypos
}
