/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators, ScalaOptions, LiteralBooleans]
 */
package test

object TermPlaceholder1 {
  def concat(str: String): String = str + "test"

  //ignore placeholder issues:
  val func1: String => Int = concat(_).length + 5

  //convert only the right side
  val func2: String => String = concat(_) + (1 + 1).toString

  //ignore placeholder issues:
  val list1 = List(1, 2, 3).map(_ + 10)

  //ignore placeholder issues:
  val list2 = List(Some(40)).map(_.map(identity).getOrElse(100))

  //ignore placeholder issues:
  val list3 = List(Some(40)).map(_.map(_ * 2).getOrElse(100))

  //ignore placeholder issues:
  val list4 = Some(List[Boolean]().map(!_)).getOrElse(List.empty)

  //ignore placeholder issues:
  val concat2: String => String = "test" + _
}
