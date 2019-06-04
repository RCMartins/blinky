package test

object ArithmeticOperators {

  val n1 = if (sys.props.contains("SCALA_MUTATION_1")) 9 + 1 + 2 - 3 else if (sys.props.contains("SCALA_MUTATION_2")) 9 + 1 - 2 + 3 else if (sys.props.contains("SCALA_MUTATION_3")) 9 - 1 + 2 + 3 else 9 + 1 + 2 + 3
  val n2 = if (sys.props.contains("SCALA_MUTATION_4")) 9 + 50 else 9 - 50
  val n3 = if (sys.props.contains("SCALA_MUTATION_5")) 9 / 50 else 9 * 50
  val n4 = if (sys.props.contains("SCALA_MUTATION_6")) 9 * 3 else 9 / 3

}
