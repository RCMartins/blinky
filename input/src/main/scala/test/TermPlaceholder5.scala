/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ConditionalExpressions]
 */
package test

object TermPlaceholder5 {

  def isLower(c: Char): Boolean = c.isLower

  val value1 = "aBc".count(!isLower(_))

}
