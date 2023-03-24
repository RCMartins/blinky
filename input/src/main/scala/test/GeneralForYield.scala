/*
rule = Blinky
Blinky.filesToMutate = [all]
 */
package test

object GeneralForYield {

  val list: IndexedSeq[String] =
    for {
      char <- 'a' to ('x' + 1).toChar
      if char != 'q'
      between = s"'$char'"
      case n: Int <- 1 to 1 + 1
    } yield s"Letter $between is cool #$n"

}
