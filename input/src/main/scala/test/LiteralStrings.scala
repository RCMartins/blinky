/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = LiteralStrings
 */
package test

object LiteralStrings {
  val string1 = ""
  val string2 = "a cool string"
  val string3 = string2 + ", or is it?"
  val string4 = "mutated!"
  val string5 = s""
  val string6 = s"$string2-test"
}
