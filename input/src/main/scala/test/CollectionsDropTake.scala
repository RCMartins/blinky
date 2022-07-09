/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ { Collections = [Take, Drop] } ]
 */
package test

object CollectionsDropTake {
  val col1a = List("a", "b").take(5)
  val col1b = List("a", "b").drop(3)

  val col2a = Seq(1, 2).take(5)
  val col2b = Seq(1, 2).drop(3)

  val col3a = Vector('a', 'b').take(5)
  val col3b = Vector('a', 'b').drop(3)

  val col4a = Array(true, false).take(5)
  val col4b = Array(true, false).drop(3)

}
