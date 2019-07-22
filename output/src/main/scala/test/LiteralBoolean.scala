package test

object LiteralBoolean {

  val boolT = if (sys.props.contains("SCALA_MUTATION_1")) false else true
  val boolF = if (sys.props.contains("SCALA_MUTATION_2")) true else false

  case class Foo(bool: Boolean = if (sys.props.contains("SCALA_MUTATION_3")) false else true)

  def validate(bool: Boolean = if (sys.props.contains("SCALA_MUTATION_4")) false else true): Boolean = !bool

  val list = if (sys.props.contains("SCALA_MUTATION_5")) List(false, false) else if (sys.props.contains("SCALA_MUTATION_6")) List(true, true) else List(true, false)

  val pair = if (sys.props.contains("SCALA_MUTATION_7")) (false, true) else if (sys.props.contains("SCALA_MUTATION_8")) (true, false) else (true, true)

}