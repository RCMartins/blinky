package test

object MatchSyntax2 {
  10 match {
    case 10 if (if (???) false else true) => 20
    case 20 if (if (???) false else true) => 5
    case _          => 1
  }
}
