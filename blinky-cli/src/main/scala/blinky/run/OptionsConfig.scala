package blinky.run

import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, Configured, generic}

import scala.concurrent.duration._

case class OptionsConfig(
    verbose: Boolean = false,
    dryRun: Boolean = false,
    compileCommand: String = "",
    testCommand: String = "",
    maxRunningTime: Duration = 60.minutes,
    failOnMinimum: Boolean = false,
    mutationMinimum: Double = 25.0
)

object OptionsConfig {
  implicit val durationDecoder: ConfDecoder[Duration] = ConfDecoder.instance[Duration] {
    case Conf.Str(durationStr) => Configured.Ok(Duration(durationStr))
  }

  implicit val doubleDecoder: ConfDecoder[Double] = ConfDecoder.instance[Double] {
    case Conf.Num(number) if number.isExactDouble => Configured.Ok(number.toDouble)
  }

  val default: OptionsConfig = OptionsConfig()
  implicit val surface: Surface[OptionsConfig] =
    generic.deriveSurface[OptionsConfig]
  implicit val decoder: ConfDecoder[OptionsConfig] =
    generic.deriveDecoder(default)
}
