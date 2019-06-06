package test

object ScalaOption {

  val op: Option[String] = Some("string")
  val op1 = if (sys.props.contains("SCALA_MUTATION_1")) "" else op.getOrElse("")
  val op2 = if (sys.props.contains("SCALA_MUTATION_2")) op.forall(_.startsWith("str")) else op.exists(_.startsWith("str"))
  val op3 = if (sys.props.contains("SCALA_MUTATION_3")) op.exists(_.contains("ing")) else op.forall(_.contains("ing"))
  val op4 = op.map(_ + "!")
  val op5 = if (sys.props.contains("SCALA_MUTATION_4")) op.nonEmpty else op.isDefined
  val op6 = if (sys.props.contains("SCALA_MUTATION_5")) op.nonEmpty else op.isEmpty
  val op7 = if (sys.props.contains("SCALA_MUTATION_6")) op.isEmpty else op.nonEmpty
  val op8 = op.get
  val op9 = if (sys.props.contains("SCALA_MUTATION_7")) "" else op.fold("")(_ * 3)
  val op10 = if (sys.props.contains("SCALA_MUTATION_8")) op else if (sys.props.contains("SCALA_MUTATION_9")) Some("test") else op.orElse(Some("test"))
  val op11 = if (sys.props.contains("SCALA_MUTATION_10")) null else op.orNull
  val op12 = if (sys.props.contains("SCALA_MUTATION_11")) op else if (sys.props.contains("SCALA_MUTATION_12")) op.filterNot(_.length < 3) else op.filter(_.length < 3)
  val op13 = if (sys.props.contains("SCALA_MUTATION_13")) op else if (sys.props.contains("SCALA_MUTATION_14")) op.filter(_.length >= 5) else op.filterNot(_.length >= 5)
  val op14 = if (sys.props.contains("SCALA_MUTATION_15")) true else if (sys.props.contains("SCALA_MUTATION_16")) false else op.contains("test")

}
