package blinky.run

import metaconfig.generic.Surface
import metaconfig.typesafeconfig._
import metaconfig.{Conf, ConfDecoder, generic}
import blinky.v0.BlinkyConfig

case class MutationsConfig(
    projectPath: String,
    sourceCodePath: String,
    filesToMutate: String,
    conf: BlinkyConfig,
    blinkyVersion: String = "0.1.0",
    testCommand: String = "test",
    options: OptionsConfig = OptionsConfig()
)

object MutationsConfig {
  val default = MutationsConfig("", "", "", BlinkyConfig(""))
  implicit val surface: Surface[MutationsConfig] =
    generic.deriveSurface[MutationsConfig]
  implicit val decoder: ConfDecoder[MutationsConfig] =
    generic.deriveDecoder(default)

  def read(conf: String): MutationsConfig =
    decoder.read(Conf.parseString(conf)).get
}
