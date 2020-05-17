package blinky.run

import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, Configured, generic}

import scala.concurrent.duration._

case class OptionsConfig(
    verbose: Boolean,
    dryRun: Boolean,
    compileCommand: String,
    testCommand: String,
    maxRunningTime: Duration,
    failOnMinimum: Boolean,
    mutationMinimum: Double
)

object OptionsConfig {
  val default: OptionsConfig = OptionsConfig(
    verbose = false,
    dryRun = false,
    compileCommand = "",
    testCommand = "",
    maxRunningTime = 60.minutes,
    failOnMinimum = false,
    mutationMinimum = 25.0
  )

  implicit val durationDecoder: ConfDecoder[Duration] = ConfDecoder.instance[Duration] {
    case Conf.Str(durationStr) => Configured.Ok(Duration(durationStr))
  }

  implicit val doubleDecoder: ConfDecoder[Double] = ConfDecoder.instance[Double] {
    case Conf.Num(number) => Configured.Ok(number.toDouble)
  }

  implicit val surface: Surface[OptionsConfig] =
    generic.deriveSurface[OptionsConfig]
  implicit val decoder: ConfDecoder[OptionsConfig] =
    generic.deriveDecoder(default).noTypos
}
