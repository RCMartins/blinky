/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = ScalaStrings
 */
package test

import java.util.Locale

object StringMutator {
  val string1 = " Foo ".trim

  val string2 = string1 + ", or is it?" + " who knows?"
  val string2b = string1.concat(", or is it?")

  val string3 = string1.toUpperCase
  val string3b = string1.toUpperCase(Locale.ENGLISH)

  val string4 = string1.toLowerCase
  val string4b = string1.toLowerCase(Locale.ENGLISH)

  val string5 = string1.capitalize

  val string6 = string1.stripPrefix("F")
  val string7 = string1.stripSuffix("oo")

  val string8 = string1.map(char => char + 1) // should ignore
  val string8b = string1.map(char => char.toUpper)

  val string9 = string1.flatMap(char => s"$char$char")

  val string10 = string1.drop(2)

  val string11 = string1.take(3)

  val string12 = string1.dropWhile(char => char.isLetter)

  val string13 = string1.takeWhile(char => char.isLetter)

  val string14 = "tacocat".reverse

}
