package test

object GeneralSyntax4 {

  case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)

  val foo1 = if (sys.props.contains("SCALA_MUTATION_1")) true else if (sys.props.contains("SCALA_MUTATION_2")) false else if (sys.props.contains("SCALA_MUTATION_3")) Some(2).contains(Foo(1 - 1, 2 + 2)(3 + 3, 4 + 4).value1) else if (sys.props.contains("SCALA_MUTATION_4")) Some(2).contains(Foo(1 + 1, 2 - 2)(3 + 3, 4 + 4).value1) else if (sys.props.contains("SCALA_MUTATION_5")) Some(2).contains(Foo(1 + 1, 2 + 2)(3 - 3, 4 + 4).value1) else if (sys.props.contains("SCALA_MUTATION_6")) Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 - 4).value1) else Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)

  val some1 = Some("value")

  val pair1 = "str" -> (if (sys.props.contains("SCALA_MUTATION_7")) null else some1.orNull[String])

}
