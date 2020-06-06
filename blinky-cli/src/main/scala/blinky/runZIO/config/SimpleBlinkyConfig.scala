package blinky.runZIO.config

import blinky.v0.{BlinkyConfig, Mutator, Mutators}
import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, ConfEncoder, generic}

case class SimpleBlinkyConfig(
    enabled: Mutators,
    disabled: Mutators
)

object SimpleBlinkyConfig {
  val default: SimpleBlinkyConfig = SimpleBlinkyConfig(
    enabled = Mutators.all,
    disabled = Mutators(Nil)
  )

  implicit val surface: Surface[SimpleBlinkyConfig] =
    generic.deriveSurface[SimpleBlinkyConfig]
  implicit val decoder: ConfDecoder[SimpleBlinkyConfig] =
    generic.deriveDecoder(default)

  implicit val mutatorEncoder: ConfEncoder[Mutator] =
    (value: Mutator) => Conf.Str(value.name)
  implicit val mutatorsEncoder: ConfEncoder[Mutators] =
    (value: Mutators) => ConfEncoder[List[Mutator]].write(value.mutations)
  implicit val blinkyConfigEncoder: ConfEncoder[BlinkyConfig] =
    generic.deriveEncoder[BlinkyConfig]
}
