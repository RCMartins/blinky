package test

object LiteralStrings {
  val string1 = if (???) "mutated!" else ""
  val string2 = if (???) "" else if (???) "mutated!" else "a cool string"
  val string3 = if (???) "mutated!" else if (???) "" else string2 + ", or is it?"
  val string4 = if (???) "" else "mutated!"
  val string5 = if (???) "mutated!" else s""
  val string6 = if (???) "" else if (???) "mutated!" else s"$string2-test"
  val string7 = if (???) "" else if (???) "mutated!" else s"$string3"
  val string8 = if (???) "" else if (???) "mutated!" else s"test"
  val string9 = if (???) "mutated!" else f""
  val string10 = if (???) "" else if (???) "mutated!" else f"$string2%20s-test"
  val string11 = if (???) "mutated!" else raw""
  val string12 = if (???) "" else if (???) "mutated!" else raw"$string2\test"

  val "string13" = if (???) "" else if (???) "mutated!" else "string13"
  var "string14" = if (???) "" else if (???) "mutated!" else "string14"
}
