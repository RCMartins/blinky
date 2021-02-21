/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [LiteralStrings, ConditionalExpressions, Collections]
 */
package test

object ParensTest {

  val seq1: Int = Seq(100).size

  val bool = true

  val result =
    seq1 match {
      case _ if !bool => 10
      case _          => 20
    }

  val map = Map("test" -> bool)

}
