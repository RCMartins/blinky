package test

object ParensTest {

  val seq1: Int = (if (???) Seq() else Seq("string")).size

  val bool = true

  val result =
    seq1 match {
      case _ if (if (???) bool else !bool) => 10
      case _          => 20
    }

}
