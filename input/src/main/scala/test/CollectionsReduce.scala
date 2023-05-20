/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [Collections.ReduceOption]
 */
package test

object CollectionsReduce {
  val col1 = List("a", "b", "c").reduceOption((a, b) => a * 2 + b)

  val col2 = Seq[Int]().reduceOption((a, b) => a + b)
}
