/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.disabledMutators = [
  LiteralBooleans
  ArithmeticOperators.IntMulToDiv
  ScalaOptions.OrElse
]
 */
package test

object DisabledMutations {
  val v1 = true
  val v2 = false
  val v3 = 100 * 5
  val v4 = v1 || v2
  val v5 = Some(100).filter(value => value > 50)
  val v6 = v5.orElse(Some(50))
}
