package test

object MatchSyntax1 {

  val value1: Option[Int] = Some(20)
  val value2: Option[Int] = Some(30)
  val value3 =
    if (_root_.scala.sys.props.contains("SCALA_MUTATION_1")) (value1.fold(value2)(x => Some(x + 4)) match {
  case None =>
    2 + 3
  case Some(v) =>
    v
}) - 10 else if (_root_.scala.sys.props.contains("SCALA_MUTATION_2")) (value2 match {
  case None =>
    2 + 3
  case Some(v) =>
    v
}) + 10 else if (_root_.scala.sys.props.contains("SCALA_MUTATION_3")) (value1.fold(value2)(x => Some(x - 4)) match {
  case None =>
    2 + 3
  case Some(v) =>
    v
}) + 10 else if (_root_.scala.sys.props.contains("SCALA_MUTATION_4")) (value1.fold(value2)(x => Some(x + 4)) match {
  case None =>
    2 - 3
  case Some(v) =>
    v
}) + 10 else (value1.fold(value2)(x => Some(x + 4)) match {
  case None =>
    2 + 3
  case Some(v) =>
    v
}) + 10

}
