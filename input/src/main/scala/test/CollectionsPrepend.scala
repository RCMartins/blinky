/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [Collections.Prepend]
 */
package test

object CollectionsPrepend {
  val list1 = 1 :: List(2, 3)
  val list2 = 0 :: list1
  val list3 = list1.prepended(2)

  val seq1 = Seq(1).prepended(2)
  val seq2 = scala.Seq(1).prepended(2)
  val seq3 = scala.collection.mutable.Seq(1).prepended(2)
  val seq4 = scala.collection.immutable.Seq(1).prepended(2)
  val seq5 = scala.collection.immutable.ArraySeq(1).prepended(2)

  val vec = Vector(1).prepended(2)

  val arr = Array(1).prepended(2)
}
