package blinky.run.config

import better.files.File

import scala.util.Try

case class MutationsConfigValidated(
    projectPath: File,
    filesToMutate: String,
    filesToExclude: String,
    mutators: SimpleBlinkyConfig,
    options: OptionsConfig
)

object MutationsConfigValidated {

  def validate(config: MutationsConfig)(pwd: File): Either[String, MutationsConfigValidated] = {
    val projectPath = File(pwd.path.resolve(config.projectPath))
    if (config.options.mutationMinimum < 0.0 || config.options.mutationMinimum > 100.0)
      Left("mutationMinimum value is invalid. It should be a number between 0 and 100.")
    else if (!projectPath.exists)
      Left(s"--projectPath '$projectPath' does not exist.")
    else {
      val filesToMutateEither: Either[String, String] = {
        val filesToMutate: File =
          Try(File(config.filesToMutate))
            .filter(_.exists)
            .getOrElse(projectPath / config.filesToMutate)

        if (filesToMutate.exists)
          Right(config.filesToMutate)
        else if (filesToMutate.extension.isEmpty && File(filesToMutate.toString + ".scala").exists)
          Right(config.filesToMutate + ".scala")
        else
          Left(s"--filesToMutate '${config.filesToMutate}' does not exist.")
      }

      filesToMutateEither.map(filesToMutate =>
        MutationsConfigValidated(
          projectPath,
          filesToMutate,
          config.filesToExclude,
          config.mutators,
          config.options
        )
      )
    }
  }

}
