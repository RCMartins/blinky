package test

object StringMutator {
  val string1 = if (???) " foo " else " foo ".trim
  val string2 = if (???) "mutated!" else if (???) "" else string1 + ", or is it?"
}
