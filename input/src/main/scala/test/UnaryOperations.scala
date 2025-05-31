/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [LiteralBooleans, ConditionalExpressions]
 */
package test

object UnaryOperations {
  val bool1 = false
  val bool2 = !bool1 || true
}
