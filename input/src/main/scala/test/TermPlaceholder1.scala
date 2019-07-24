/*
rule = MutateCode
MutateCode.activeMutators = [ArithmeticOperators]
*/
package test

object TermPlaceholder1 {

  def concat(str: String): String = str + "test"

  //ignore
  val func1: String => Int = concat(_).length + 5

}
