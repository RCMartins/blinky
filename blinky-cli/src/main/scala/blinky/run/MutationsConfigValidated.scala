//package blinky.run
//
//import better.files.File
//
//case class MutationsConfigValidated(
//    projectPath: File,
//    filesToMutate: String,
//    filesToExclude: String,
//    mutators: SimpleBlinkyConfig,
//    options: OptionsConfig
//)
//
//object MutationsConfigValidated {
//
//  def validate(config: MutationsConfig)(pwd: File): Either[String, MutationsConfigValidated] = {
//    val projectPath = File(pwd.path.resolve(config.projectPath))
//    if (config.options.mutationMinimum < 0.0 || config.options.mutationMinimum > 100.0)
//      Left("mutationMinimum value is invalid. It should be a number between 0 and 100.")
//    else if (!projectPath.exists)
//      Left(s"--projectPath '$projectPath' does not exists.")
//    else
//      Right(
//        MutationsConfigValidated(
//          projectPath,
//          config.filesToMutate,
//          config.filesToExclude,
//          config.mutators,
//          config.options
//        )
//      )
//  }
//
//}
