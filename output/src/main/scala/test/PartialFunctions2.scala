package test

object PartialFunctions2 {
  List(10, 20, 30).filter {
    case 10 => true
    case 20 => false
    case 30 => true
  }
}
