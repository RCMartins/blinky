package test

object LiteralString {

  val string1 = if (sys.props.contains("SCALA_MUTATION_1")) "mutated!" else ""
  val string2 = if (sys.props.contains("SCALA_MUTATION_2")) "" else if (sys.props.contains("SCALA_MUTATION_3")) "mutated!" else "a cool string"
  val string3 = if (sys.props.contains("SCALA_MUTATION_4")) "mutated!" else if (sys.props.contains("SCALA_MUTATION_5")) "" else string2 + ", or is it?"

}