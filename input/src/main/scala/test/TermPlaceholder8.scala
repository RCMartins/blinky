/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, LiteralStrings, ScalaStrings]
 */
package test

object TermPlaceholder8 {

  def trimOpt(optional: Option[String]): String =
    optional.map(_.trim).getOrElse("")

}
