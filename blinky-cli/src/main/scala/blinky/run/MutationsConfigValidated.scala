package blinky.run

import better.files.File

case class MutationsConfigValidated(
    projectPath: File,
    filesToMutate: String,
    filesToExclude: String,
    mutators: SimpleBlinkyConfig,
    options: OptionsConfig
)

object MutationsConfigValidated {

  def validate(config: MutationsConfig): Either[String, MutationsConfigValidated] = {
    if (
      config.options.failOnMinimum && (config.options.mutationMinimum < 0.0 || config.options.mutationMinimum > 100.0)
    ) {
      Left("mutationMinimum value is invalid. It should be a number between 0 and 100.")
    } else
      Right(
        MutationsConfigValidated(
          config.projectPath,
          config.filesToMutate,
          config.filesToExclude,
          config.mutators,
          config.options
        )
      )
  }

}
