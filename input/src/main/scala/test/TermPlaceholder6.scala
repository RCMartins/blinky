/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions.OrElse, ArithmeticOperators.IntPlusToMinus, ScalaStrings.Trim]
 */
package test

object TermPlaceholder6 {

  def or321mapped: Option[Int] => Option[Int] = _.orElse(Some(321)).map(n => n * 2)

  def or321mapped2: Option[Int] => Option[Int] = _.orElse(Some(321)).map(_ + 1)

  def or321mapped3: Option[String] => Option[Int] = _.orElse(Some("abc")).map(_.trim.hashCode())

}
