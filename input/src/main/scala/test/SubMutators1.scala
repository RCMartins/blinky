/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [
  ArithmeticOperators.IntPlusToMinus
  ArithmeticOperators.IntMulToDiv
]
 */
package test

object SubMutators1 {
  val n1 = 2 + 1
  val n2 = 2 - 1
  val n3 = 2 * 1
  val n4 = 2 / 1
}
