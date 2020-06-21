package blinky.run.config

import com.softwaremill.quicklens._
import metaconfig.generic.Surface
import metaconfig.typesafeconfig._
import metaconfig.{Conf, ConfDecoder, ConfError, generic}

case class MutationsConfig(
    projectPath: String,
    projectName: String,
    filesToMutate: String,
    filesToExclude: String,
    mutators: SimpleBlinkyConfig,
    options: OptionsConfig
)

object MutationsConfig {
  val default: MutationsConfig = MutationsConfig(
    projectPath = "",
    projectName = "",
    filesToMutate = "src/main/scala",
    filesToExclude = "",
    mutators = SimpleBlinkyConfig.default,
    options = OptionsConfig.default
  )

  implicit val surface: Surface[MutationsConfig] =
    generic.deriveSurface[MutationsConfig]
  implicit val decoder: ConfDecoder[MutationsConfig] =
    generic.deriveDecoder(default).noTypos

  def read(conf: String): Either[ConfError, MutationsConfig] =
    decoder.read(Conf.parseString(conf)).toEither.map { original =>
      val projectName = original.projectName
      original
        .modifyAll(_.options.compileCommand, _.options.testCommand)
        .setToIf(projectName.nonEmpty)(projectName)
    }
}
