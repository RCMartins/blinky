/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators, ScalaOptions]
 */
package test

object MatchSyntax1 {
  val value1: Option[Int] = Some(20)
  val value2: Option[Int] = Some(30)
  val value3 =
    (value1.fold(value2)(x => Some(x + 4)) match {
      case None    => 2 + 3
      case Some(v) => v
    }) + 10
}
