/*
rule = MutateCode
MutateCode.activeMutators = [ArithmeticOperators]
*/
package test

object TermPlaceholder1 {

  def concat(str: String): String = str + "test"

  //ignore
  val func1: String => Int = concat(_).length + 5

  //convert only the right side
  val func2: String => String = concat(_) + (1 + 1).toString

}
