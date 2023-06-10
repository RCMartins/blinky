/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [PartialFunctions.RemoveOneCase]
 */
package test

object PartialFunctionsMutators3 {
  val value1: Double =
    List(1.0, 2.0, 3.0).map {
      case n if n <= 2.0 => n.toInt
      case n             => n
    }.sum
}
