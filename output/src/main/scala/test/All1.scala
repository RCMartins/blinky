package test

object All1 {

  val op: Option[String] = if (sys.props.contains("SCALA_MUTATION_1")) Some("string").orElse(Some("test")) else if (sys.props.contains("SCALA_MUTATION_2")) Some("string").orElse(Some("test")).filterNot(_.length < 3 * 2) else if (sys.props.contains("SCALA_MUTATION_3")) Some("string").filter(_.length < 3 * 2) else if (sys.props.contains("SCALA_MUTATION_4")) Some("test").filter(_.length < 3 * 2) else if (sys.props.contains("SCALA_MUTATION_5")) Some("").orElse(Some("test")).filter(_.length < 3 * 2) else if (sys.props.contains("SCALA_MUTATION_6")) Some("mutated!").orElse(Some("test")).filter(_.length < 3 * 2) else if (sys.props.contains("SCALA_MUTATION_7")) Some("string").orElse(Some("")).filter(_.length < 3 * 2) else if (sys.props.contains("SCALA_MUTATION_8")) Some("string").orElse(Some("mutated!")).filter(_.length < 3 * 2) else if (sys.props.contains("SCALA_MUTATION_9")) Some("string").orElse(Some("test")).filter(_.length < 3 / 2) else Some("string").orElse(Some("test")).filter(_.length < 3 * 2)

}
