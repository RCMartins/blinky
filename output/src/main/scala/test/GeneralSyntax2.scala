package test

object GeneralSyntax2 {

  def str1: String = (if (sys.props.contains("SCALA_MUTATION_1")) 1 - 2 else 1 + 2) + ""

  val bool1 = !(if (sys.props.contains("SCALA_MUTATION_2")) false else true)

  def functionWithBlock: Boolean = {
    val bool = (if (sys.props.contains("SCALA_MUTATION_3")) false else true)
    !bool
  }

}
