/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [Collections]
 */
package test

object Collections {
  val seq1 = Seq(1, 2)
  val seq2 = scala.collection.mutable.Seq(1, 2)
  val seq3 = scala.collection.immutable.Seq(1, 2)
}
