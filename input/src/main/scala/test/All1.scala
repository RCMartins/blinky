/*
rule = Blinky
#Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
 */
package test

object All1 {
  val op: Option[String] = Some("string").orElse(Some("test")).filter(str => str.length < 3 * 2)
}
