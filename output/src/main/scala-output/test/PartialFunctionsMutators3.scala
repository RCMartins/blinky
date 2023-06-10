package test

object PartialFunctionsMutators3 {
  val value1: Double =
    List(1.0, 2.0, 3.0).map (if (_root_.scala.sys.env.contains("BLINKY_MUTATION_1")) {
  case n if false =>
    n.toInt
  case n =>
    n
} else if (_root_.scala.sys.env.contains("BLINKY_MUTATION_2")) {
  case n if n <= 2.0d =>
    n.toInt
  case n if false =>
    n
} else {
  case n if n <= 2.0d =>
    n.toInt
  case n =>
    n
}).sum
}