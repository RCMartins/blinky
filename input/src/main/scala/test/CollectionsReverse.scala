/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [Collections.Reverse]
 */
package test

object CollectionsReverse {
  val col1 = List("a", "b").reverse

  val col2 = Seq(1, 2).reverse

  val col3 = Vector('a', 'b').reverse

  val col4 = Array(true, false).reverse

}
