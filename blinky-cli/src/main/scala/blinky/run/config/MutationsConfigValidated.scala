package blinky.run.config

import ammonite.ops.RelPath
import better.files.File
import blinky.run.config.FileFilter.{FileName, SingleFileOrFolder}

case class MutationsConfigValidated(
    projectPath: File,
    filesToMutate: FileFilter,
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
      val filesToMutate: FileFilter = {
        val filesToMutate: File = projectPath / config.filesToMutate

        if (filesToMutate.exists)
          SingleFileOrFolder(RelPath(config.filesToMutate))
        else if (filesToMutate.extension.isEmpty && File(filesToMutate.toString + ".scala").exists)
          SingleFileOrFolder(RelPath(config.filesToMutate + ".scala"))
        else {
          FileName(config.filesToMutate)
        }
      }

      Right(
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
