package test

object LiteralString {

  val string1 = if (???) "mutated!" else ""
  val string2 = if (???) "" else if (???) "mutated!" else "a cool string"
  val string3 = if (???) "mutated!" else if (???) "" else string2 + ", or is it?"

}