/*
rule = MutateCode
MutateCode.activeMutators = [ArithmeticOperators, LiteralBoolean]
*/
package test

object GeneralSyntax2 {

  def str1: String = (1 + 2) + ""

  val bool1 = !true

  def functionWithBlock: Boolean = {
    val bool = true
    def fun(param: Int = 5 + 3): Int = param + 1
    !bool
  }

}
