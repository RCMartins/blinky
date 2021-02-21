package test

object ParensTest {

  val seq1: Int = (if (???) Seq() else Seq(100)).size

  val bool = true

  val result =
    seq1 match {
      case _ if (if (???) bool else !bool) => 10
      case _          => 20
    }

  val map = Map((if (???) "" else if (???) "mutated!" else "test") -> bool)

}
