/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, LiteralStrings, ScalaStrings, Collections]
 */
package test

object TermPlaceholder11 {

  def useF(
      f: List[String] => List[String],
      list: List[String]
  ): List[String] = f(list)

  def value: List[String] => List[String] = useF(_.reverse, _).reverse

}
