/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [Collections]
 */
package test

object Collections1 {
  val list1 = List("a", "b", "c", "d")
  val list2 = scala.List("a", "b")
  val list3 = scala.collection.immutable.List("a", "b")

  val seq1 = Seq(1, 2)
  val seq2 = scala.Seq(1, 2)
  val seq3 = scala.collection.mutable.Seq(1, 2)
  val seq4 = scala.collection.immutable.Seq(1, 2)

  val set1 = Set(1, 2)
  val set2 = scala.collection.mutable.Set(1, 2)
  val set3 = scala.collection.immutable.Set(1, 2)
}
