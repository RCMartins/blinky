package test

import java.util.Locale

object StringMutator {
  val string1 = if (???) " Foo " else " Foo ".trim

  val string2 = if (???) "mutated!" else if (???) "" else string1 + ", or is it?"
  val string2b = if (???) "mutated!" else if (???) "" else string1.concat(", or is it?")

  val string3 = if (???) string1 else string1.toUpperCase
  val string3b = if (???) string1 else string1.toUpperCase(Locale.ENGLISH)

  val string4 = if (???) string1 else string1.toLowerCase
  val string4b = if (???) string1 else string1.toUpperCase(Locale.ENGLISH)
}
