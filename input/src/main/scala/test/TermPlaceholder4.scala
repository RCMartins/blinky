/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators]
 */
package test

object TermPlaceholder4 {

  def concat2(str1: String, str2: String): String = str1 + "test" + str2

  def concat3(str1: String, str2: String, str3: String): String =
    str1 + "test" + str2 + "foo" + str3

  val func1: (String, String) => Int = concat2(_, _).length + 5

  val func2: (String, String) => String = concat2(_, _) + (1 + 1).toString

  val func3: (String, String, String) => Int = concat3(_, _, _).length + 5

}
