package test

object ConditionalExpression1 {

  val bool1 = if (sys.props.contains("SCALA_MUTATION_1")) false else true
  val bool2 = if (sys.props.contains("SCALA_MUTATION_2")) true else false
  val bool3 = if (sys.props.contains("SCALA_MUTATION_3")) bool1 || bool2 else bool1 && bool2
  val bool4 = if (sys.props.contains("SCALA_MUTATION_4")) bool3 && bool2 else bool3 || bool2
  val bool5 = if (sys.props.contains("SCALA_MUTATION_5")) bool4 else !bool4

}
