package blinky.runZIO.config

import com.softwaremill.quicklens._
import metaconfig.generic.Surface
import metaconfig.typesafeconfig._
import metaconfig.{Conf, ConfDecoder, generic}

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

  def read(conf: String): MutationsConfig = {
    val original = decoder.read(Conf.parseString(conf)).get
    val projectName = original.projectName
    original
      .modifyAll(_.options.compileCommand, _.options.testCommand)
      .setToIf(projectName.nonEmpty)(projectName)
  }
}
