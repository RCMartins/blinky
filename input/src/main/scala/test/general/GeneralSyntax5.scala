/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators, LiteralBooleans]
 */
package test.general

object GeneralSyntax5 {
  val if1 = (if (true) 1 + 7 else 2 * 5) + 10

  val tuple1 = (10 + 20, 30)._1 + 10
}
