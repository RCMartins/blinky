/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, LiteralStrings, ScalaStrings]
 */
package test

object TermPlaceholder7 {

  def addSpace(optional: Option[String]): String = optional.map(_ + " ").getOrElse("")

  def addSpaceWith(optional: Option[String]): Option[String] => String =
    _.getOrElse(optional.map(_ + " ").getOrElse(""))

}
