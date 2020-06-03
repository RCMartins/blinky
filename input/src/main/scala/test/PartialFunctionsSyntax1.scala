/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [LiteralBooleans]
 */
package test

object PartialFunctionsSyntax1 {
  List(10, 20, 30).map {
    case 10 if true => 20
    case 20 if true => 5
    case _          => 1
  }
}
