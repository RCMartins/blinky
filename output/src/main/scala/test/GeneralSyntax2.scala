package test

object GeneralSyntax2 {

  def str1: String = (if (sys.props.contains("SCALA_MUTATION_1")) 1 - 2 else 1 + 2) + ""

  def str2: String = (if (sys.props.contains("SCALA_MUTATION_2")) 1 - 2 else 1 + 2).toString

  val bool1 = !(if (sys.props.contains("SCALA_MUTATION_3")) false else true)

  def functionWithBlock: Boolean = {
    val bool = if (sys.props.contains("SCALA_MUTATION_4")) false else true
    def fun(param: Int = if (sys.props.contains("SCALA_MUTATION_5")) 5 - 3 else 5 + 3): Int = if (sys.props.contains("SCALA_MUTATION_6")) param - 1 else param + 1
    !bool
  }

}
