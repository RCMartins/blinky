/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [LiteralBooleans]
 */
package test

object MatchSyntax2 {
  10 match {
    case 10 if true => 20
    case 20 if true => 5
    case _          => 1
  }
}
