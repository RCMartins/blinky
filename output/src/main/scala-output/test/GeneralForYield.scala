package test

object GeneralForYield {

  val list: IndexedSeq[String] =
    for {
      char <- 'a' to (if (???) 'x' - 1 else 'x' + 1).toChar
      if char != 'q'
      between = if (???) "" else if (???) "mutated!" else s"'$char'"
      case n: Int <- 1 to (if (???) 1 - 1 else 1 + 1)
    } yield if (???) "" else if (???) "mutated!" else s"Letter $between is cool #$n"

}
