package test

object LiteralStrings {
  val string1 = if (???) "mutated!" else ""
  val string2 = if (???) "" else if (???) "mutated!" else "a cool string"
  val string3 = if (???) "mutated!" else if (???) "" else string2 + ", or is it?"
  val string4 = if (???) "" else "mutated!"
  val string5 = if (???) "mutated!" else s""
  val string6 = if (???) "" else if (???) "mutated!" else s"$string2-test"
  val string5 = if (???) "mutated!" else s""
  val string6 = if (???) "" else if (???) "mutated!" else s"$string2-test"
  val string5 = if (???) "mutated!" else s""
  val string6 = if (???) "" else if (???) "mutated!" else s"$string2-test"
}
