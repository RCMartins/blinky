package test

object PartialFunctionsSyntax1 {
  List(10, 20, 30).map {
    case 10 if (if (???) false else true) => 20
    case 20 if (if (???) false else true) => 5
    case _          => 1
  }
}
