import $ivy.`com.geirsson::metaconfig-core:0.8.1`
import $ivy.`com.geirsson::metaconfig-typesafe-config:0.8.1`
import $ivy.`com.typesafe.play::play-json:2.7.3`
import metaconfig.generic.Surface
import metaconfig.typesafeconfig._
import metaconfig.{Conf, ConfDecoder, ConfEncoder, ConfError, Configured, generic}
import play.api.libs.json.{Json, Reads}

import scala.concurrent.duration._
import scala.reflect.ClassTag

trait MutationType

object MutationType {

  case object LiteralBooleans extends MutationType

  case object ArithmeticOperators extends MutationType

  case object ConditionalExpressions extends MutationType

  case object LiteralStrings extends MutationType

  case object ScalaOptions extends MutationType

  val all: List[MutationType] = List(
    LiteralBooleans,
    ArithmeticOperators,
    ConditionalExpressions,
    LiteralStrings,
    ScalaOptions
  )

  val allMap = all.map(mType => mType.toString -> mType).toMap

  implicit val readerMutationType: ConfDecoder[MutationType] = fromMap(allMap)

  implicit val writerMutationType: ConfEncoder[MutationType] =
    ConfEncoder.instance[MutationType] { mType =>
      Conf.fromString(mType.toString)
    }

  // Poor mans coproduct reader
  private def fromMap[T: ClassTag](
      m: Map[String, T],
      additionalMessage: PartialFunction[String, String] = PartialFunction.empty
  ): ConfDecoder[T] =
    ConfDecoder.instance[T] {
      case Conf.Str(x) =>
        m.get(x) match {
          case Some(y) =>
            Configured.Ok(y)
          case None =>
            val available = m.keys.mkString(", ")
            val extraMsg = additionalMessage.applyOrElse(x, (_: String) => "")
            val msg =
              s"Unknown input '$x'. Expected one of: $available. $extraMsg"
            Configured.NotOk(ConfError.message(msg))
        }
    }
}

case class MutateCodeConfig(
    activeMutators: List[MutationType] = MutationType.all,
    disabledMutators: List[MutationType] = Nil
)

object MutateCodeConfig {
  val default = MutateCodeConfig()
  implicit val surface: Surface[MutateCodeConfig] =
    generic.deriveSurface[MutateCodeConfig]
  implicit val decoder: ConfDecoder[MutateCodeConfig] =
    generic.deriveDecoder(default)
  implicit val encoder: ConfEncoder[MutateCodeConfig] =
    generic.deriveEncoder[MutateCodeConfig]
}

case class OptionsConfig(
    verbose: Boolean = false,
    dryRun: Boolean = false,
    compileCommand: String = "compile",
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

  val default = OptionsConfig()
  implicit val surface: Surface[OptionsConfig] =
    generic.deriveSurface[OptionsConfig]
  implicit val decoder: ConfDecoder[OptionsConfig] =
    generic.deriveDecoder(default)
}

case class MutationsConfig(
    projectPath: String,
    sourceCodePath: String,
    filesToMutate: String,
    conf: MutateCodeConfig,
    mutateCodeVersion: String,
    testCommand: String = "test",
    options: OptionsConfig = OptionsConfig()
)

object MutationsConfig {
  val default = MutationsConfig("", "", "", MutateCodeConfig(), "")
  implicit val surface: Surface[MutationsConfig] =
    generic.deriveSurface[MutationsConfig]
  implicit val decoder: ConfDecoder[MutationsConfig] =
    generic.deriveDecoder(default)

  def read(conf: String): MutationsConfig =
    decoder.read(Conf.parseString(conf)).get
}

case class Mutant(id: Int, diff: List[String], original: String, mutated: String)

object Mutant {
  implicit val mutationReads: Reads[Mutant] = Json.reads[Mutant]
}
