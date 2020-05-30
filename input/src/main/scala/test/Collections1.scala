/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [Collections]
 */
package test

object Collections1 {
  val seq1 = Seq(1, 2)
  val seq2 = scala.collection.mutable.Seq(1, 2)
  val seq3 = scala.collection.immutable.Seq(1, 2)

  val list1 = List("a", "b")
  val list2 = scala.List("a", "b")
  val list3 = scala.collection.immutable.List("a", "b")
}
