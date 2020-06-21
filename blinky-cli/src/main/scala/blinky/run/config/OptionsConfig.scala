package blinky.run.config

import metaconfig.generic.Surface
import metaconfig.{Conf, ConfDecoder, Configured, generic}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

case class OptionsConfig(
    verbose: Boolean,
    dryRun: Boolean,
    compileCommand: String,
    testCommand: String,
    maxRunningTime: Duration,
    failOnMinimum: Boolean,
    mutationMinimum: Double,
    onlyMutateDiff: Boolean,
    multiRun: (Int, Int)
)

object OptionsConfig {
  val default: OptionsConfig = OptionsConfig(
    verbose = false,
    dryRun = false,
    compileCommand = "",
    testCommand = "",
    maxRunningTime = 60.minutes,
    failOnMinimum = false,
    mutationMinimum = 25.0,
    onlyMutateDiff = false,
    multiRun = (1, 1)
  )

  implicit val durationDecoder: ConfDecoder[Duration] = ConfDecoder.instance[Duration] {
    case Conf.Str(durationStr) => Configured.Ok(Duration(durationStr))
  }

  implicit val doubleDecoder: ConfDecoder[Double] = ConfDecoder.instance[Double] {
    case Conf.Num(number) => Configured.Ok(number.toDouble)
  }

  def stringToMultiRunParser: String => Either[String, (Int, Int)] =
    (str: String) =>
      Try(str.split("/").toList.map(_.toInt)) match {
        case Success(List(index, amount)) if index >= 1 && amount >= index =>
          Right((index, amount))
        case Success(_) =>
          Left("Invalid values, they should be >= 1")
        case Failure(_) =>
          Left("Invalid value, should be in 'int/int' format")
      }

  implicit val multiRunDecoder: ConfDecoder[(Int, Int)] = ConfDecoder.instance[(Int, Int)] {
    case Conf.Str(multiRunStr) =>
      stringToMultiRunParser(multiRunStr) match {
        case Right(multiRunValue) => Configured.Ok(multiRunValue)
        case Left(message)        => Configured.error(message)
      }
  }

  implicit val surface: Surface[OptionsConfig] =
    generic.deriveSurface[OptionsConfig]
  implicit val decoder: ConfDecoder[OptionsConfig] =
    generic.deriveDecoder(default).noTypos
}
