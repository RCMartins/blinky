/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ConditionalExpressions, ScalaOptions.OrElse]
 */
package test

object TermPlaceholder5 {

  def isLower(c: Char): Boolean = c.isLower

  val value1 = "aBc".count(!isLower(_))

  def or321: Option[Int] => Option[Int] = _.orElse(Some(321))

  val value2 = or321(Some(123))

  val value3 = or321(None)

}
