/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators, ScalaOptions, LiteralBooleans]
 */
package test

object TermPlaceholder3 {

  val list1 = List(1, 2, 3).map(_ + 10)

  val list2 = List(Some(40)).map(_.map(identity).getOrElse(100))

  val list3 = List(Some(40)).map(_.map(_ * 2).getOrElse(200))

  val list4 = Some(List[Boolean]().map(!_)).getOrElse(List.empty)

}
