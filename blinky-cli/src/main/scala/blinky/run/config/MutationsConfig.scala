package blinky.run.config

import com.softwaremill.quicklens._
import zio._
import zio.config.magnolia._
import zio.config.typesafe._

case class MutationsConfig(
    projectPath: String,
    projectName: String,
    filesToMutate: String,
    filesToExclude: String,
    mutators: SimpleBlinkyConfig,
//    options: OptionsConfig
)

object MutationsConfig {
//  val default: MutationsConfig = MutationsConfig(
//    projectPath = "",
//    projectName = "",
//    filesToMutate = "src/main/scala",
//    filesToExclude = "",
//    mutators = SimpleBlinkyConfig.default,
//    options = OptionsConfig.default
//  )

//  implicit val mutationsConfig: DeriveConfig[MutationsConfig] =
//    DeriveConfig[String].map(string => AwsRegion.from(string))

//  private val simpleBlinkyConfigDescriptor: DeriveConfig[SimpleBlinkyConfig] =
//    deriveConfig[SimpleBlinkyConfig]

  def read(conf: String): Task[MutationsConfig] = {
    val hoconSource: ConfigProvider =
      ConfigProvider.fromHoconString(conf)

    hoconSource
      .load(deriveConfig[MutationsConfig])
      .mapBoth(
        error => new Exception(error.toString),
        original => {
          val projectName = original.projectName
          original
            .modifyAll(_.options.compileCommand, _.options.testCommand)
            .setToIf(projectName.nonEmpty)(projectName)
        }
      )

    // decoder.read(Conf.parseString(conf)).toEither.map
  }
}
