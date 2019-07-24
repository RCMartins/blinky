package test

object GeneralSyntax1 {

  case class Foo(bool: Boolean = if (sys.props.contains("SCALA_MUTATION_1")) false else true)

  def validate(bool: Boolean = if (sys.props.contains("SCALA_MUTATION_2")) false else true): Boolean = !bool

  val list = if (sys.props.contains("SCALA_MUTATION_3")) List(false, false) else if (sys.props.contains("SCALA_MUTATION_4")) List(true, true) else List(true, false)

  val pair = if (sys.props.contains("SCALA_MUTATION_5")) (false, true) else if (sys.props.contains("SCALA_MUTATION_6")) (true, false) else (true, true)

  val mat = if (sys.props.contains("SCALA_MUTATION_7")) (1, 2) match {
  case (1, 2) => false
  case (2, 1) => true
  case _ => false
} else if (sys.props.contains("SCALA_MUTATION_8")) (1, 2) match {
  case (1, 2) => true
  case (2, 1) => false
  case _ => false
} else if (sys.props.contains("SCALA_MUTATION_9")) (1, 2) match {
  case (1, 2) => true
  case (2, 1) => true
  case _ => true
} else (1, 2) match {
  case (1, 2) => true
  case (2, 1) => true
  case _ => false
}

  val list2 = if (sys.props.contains("SCALA_MUTATION_10")) list.map(_ => false) else list.map(_ => true)

  val callWithNamedParams = if (sys.props.contains("SCALA_MUTATION_11")) validate(bool = true) else validate(bool = false)

}