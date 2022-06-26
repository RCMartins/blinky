package blinky.run.config

import blinky.v0.MutantRange
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
    mutant: Seq[MutantRange],
    multiRun: (Int, Int),
    timeoutFactor: Double,
    timeout: Duration,
    testInOrder: Boolean
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
    mutant = Seq(MutantRange(1, Int.MaxValue)),
    multiRun = (1, 1),
    timeoutFactor = 1.5,
    timeout = 5.second,
    testInOrder = false
  )

  implicit val durationDecoder: ConfDecoder[Duration] =
    ConfDecoder.fromPartial[Duration]("Duration string") {
      case Conf.Str(durationStr) if Try(Duration(durationStr)).isSuccess =>
        Configured.Ok(Duration(durationStr))
    }

  implicit val doubleDecoder: ConfDecoder[Double] =
    ConfDecoder.fromPartial[Double]("Number") { case Conf.Num(number) =>
      Configured.Ok(number.toDouble)
    }

  private val InvalidMultiRunConfIndex: String =
    "Invalid index value, should be >= 1"
  private val InvalidMultiRunConfAmount: String =
    "Invalid amount, should be greater or equal than index"
  private val InvalidMultiRunConfFormat: String =
    "Invalid value, should be a String in 'int/int' format"
  private val InvalidMultiRunConfFormatShort: String =
    "String in 'int/int' format"

  def stringToMultiRunParser: String => Either[String, (Int, Int)] =
    (str: String) =>
      Try(str.split("/").toList.map(_.toInt)) match {
        case Success(List(index, _)) if index < 1 =>
          Left(InvalidMultiRunConfIndex)
        case Success(List(index, amount)) if index > amount =>
          Left(InvalidMultiRunConfAmount)
        case Success(List(index, amount)) =>
          Right((index, amount))
        case Success(_) | Failure(_) =>
          Left(InvalidMultiRunConfFormat)
      }

  implicit val multiRunDecoder: ConfDecoder[(Int, Int)] =
    ConfDecoder.fromPartial[(Int, Int)](InvalidMultiRunConfFormatShort) {
      case conf @ Conf.Str(multiRunStr) =>
        stringToMultiRunParser(multiRunStr) match {
          case Right(multiRunValue) => Configured.Ok(multiRunValue)
          case Left(message)        => Configured.typeMismatch(message, conf)
        }
    }

  implicit val surface: Surface[OptionsConfig] =
    generic.deriveSurface[OptionsConfig]
  implicit val decoder: ConfDecoder[OptionsConfig] =
    generic.deriveDecoder(default).noTypos
}
