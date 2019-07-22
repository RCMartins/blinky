package test

object ConditionalExpressions1 {

  val bool1 = true
  val bool2 = false
  val bool3 = if (sys.props.contains("SCALA_MUTATION_1")) bool1 || bool2 else bool1 && bool2
  val bool4 = if (sys.props.contains("SCALA_MUTATION_2")) bool3 && bool2 else bool3 || bool2
  val bool5 = if (sys.props.contains("SCALA_MUTATION_3")) bool4 else !bool4

}
