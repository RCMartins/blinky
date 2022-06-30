/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators]
 */
package test

object ArithmeticOperators {
  val n1 = 9 + 1 + 2 + 3
  val n2 = 9 - 50
  val n3 = 9 * 50
  val n4 = 9 / 3

  val cn1 = 'y' + 6
  val cn2 = 'f' - 3
  val cn3 = 'u' * 3
  val cn4 = 'z' / 3
}
