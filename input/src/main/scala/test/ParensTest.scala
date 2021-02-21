/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ConditionalExpressions, Collections]
 */
package test

object ParensTest {

  val seq1: Int = Seq("string").size

  val bool = true

  val result =
    seq1 match {
      case _ if !bool => 10
      case _          => 20
    }

}
