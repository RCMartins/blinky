/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators]
 */
package test

object TermPlaceholder1 {

  def concat(str: String): String = str + "test"

  val func1: String => Int = concat(_).length + 5

  val func2: String => String = concat(_) + (1 + 1).toString

}
