/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [LiteralBooleans]
 */
package test

object LiteralBooleans {
  val boolT1 = true
  val boolT2 = !false
  val boolF1 = false
  val boolF2 = !true
}
