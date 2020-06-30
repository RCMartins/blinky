/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = ScalaStrings
 */
package test

object StringMutator {
  val string1 = " foo ".trim
  val string2 = string1 + ", or is it?"
}
