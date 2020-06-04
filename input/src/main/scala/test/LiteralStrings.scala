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
  val string7 = s"$string3"
  val string8 = s"test"
  val string9 = f""
  val string10 = f"$string2%20s-test"
  val string11 = raw""
  val string12 = raw"$string2\test"
}
