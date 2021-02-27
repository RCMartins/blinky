/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators]
 */
package test

object TermPlaceholder4 {

  def concat3(str1: String, str2: String): String = str1 + "test" + str2

  val func1: (String, String) => Int = concat3(_, _).length + 5

  val func2: (String, String) => String = concat3(_, _) + (1 + 1).toString

}
