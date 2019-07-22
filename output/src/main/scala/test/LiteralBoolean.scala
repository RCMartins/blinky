package test

object LiteralBoolean {

  val boolT = if (sys.props.contains("SCALA_MUTATION_1")) false else true
  val boolF = if (sys.props.contains("SCALA_MUTATION_2")) true else false

}