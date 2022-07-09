package test

import java.util.Locale

object StringMutator {
  val string1 = if (???) " Foo " else " Foo ".trim

  val string2 = if (???) "mutated!" else if (???) "" else string1 + ", or is it?" + " who knows?"
  val string2b = if (???) "mutated!" else if (???) "" else string1.concat(", or is it?")

  val string3 = if (???) string1 else string1.toUpperCase
  val string3b = if (???) string1 else string1.toUpperCase(Locale.ENGLISH)

  val string4 = if (???) string1 else string1.toLowerCase
  val string4b = if (???) string1 else string1.toLowerCase(Locale.ENGLISH)

  val string5 = if (???) string1 else string1.capitalize

  val string6 = if (???) string1 else string1.stripPrefix("F")
  val string7 = if (???) string1 else string1.stripSuffix("oo")

  val string8 = string1.map(char => char + 1) // should ignore
  val string8b = if (???) string1 else string1.map(char => char.toUpper)

  val string9 = if (???) string1 else string1.flatMap(char => s"$char$char")

  val string10 = if (???) string1 else string1.drop(2)

  val string11 = if (???) string1 else string1.take(3)

  val string12 = if (???) string1 else string1.dropWhile(char => char.isLetter)

  val string13 = if (???) string1 else string1.takeWhile(char => char.isLetter)

  val string14 = if (???) "tacocat" else "tacocat".reverse

}
