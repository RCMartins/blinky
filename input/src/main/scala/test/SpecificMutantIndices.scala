/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.specificMutants = "2,4,6-8,12"
 */
package test

object SpecificMutantIndices {
  val string1 = ""
  val string2 = "a cool string"
  val string4 = "mutated!"
  val string5 = s""
  val string6 = s"$string2-test"
  val string7 = s"$string2"
  val string8 = s"test"
  val string9 = f""
}
