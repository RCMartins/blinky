/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [PartialFunctions]
 */
package test

object PartialFunctions2 {
  List(10, 20, 30).map {
    case 10 => true
    case 20 => false
    case 30 => true
    case _  => false
  }
}
