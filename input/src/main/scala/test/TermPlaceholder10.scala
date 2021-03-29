/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, LiteralStrings, ScalaStrings]
 */
package test

object TermPlaceholder10 {

  def trimList(list: List[String]): List[String] = list.map(_.trim.trim)

}
