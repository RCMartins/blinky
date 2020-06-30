/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = ScalaStrings
 */
package test

import java.util.Locale

object StringMutator {
  val string1 = " Foo ".trim

  val string2 = string1 + ", or is it?"
  val string2b = string1.concat(", or is it?")

  val string3 = string1.toUpperCase
  val string3b = string1.toUpperCase(Locale.ENGLISH)

  val string4 = string1.toLowerCase
  val string4b = string1.toUpperCase(Locale.ENGLISH)
}
