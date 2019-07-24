package test

object GeneralSyntax1 {

  case class Foo(bool: Boolean = if (sys.props.contains("SCALA_MUTATION_1")) false else true)

  def validate(bool: Boolean = if (sys.props.contains("SCALA_MUTATION_2")) false else true): Boolean = !bool

  val list = List((if (sys.props.contains("SCALA_MUTATION_3")) false else true), (if (sys.props.contains("SCALA_MUTATION_4")) true else false))

  val pair = ((if (sys.props.contains("SCALA_MUTATION_5")) false else true), (if (sys.props.contains("SCALA_MUTATION_6")) false else true))

  val mat = (1, 2) match {
    case (1, 2) => (if (sys.props.contains("SCALA_MUTATION_7")) false else true)
    case (2, 1) => (if (sys.props.contains("SCALA_MUTATION_8")) false else true)
    case _ => (if (sys.props.contains("SCALA_MUTATION_9")) true else false)
  }

  val list2 = list.map(_ => (if (sys.props.contains("SCALA_MUTATION_10")) false else true))

  val callWithNamedParams = validate(bool = (if (sys.props.contains("SCALA_MUTATION_11")) true else false))

}