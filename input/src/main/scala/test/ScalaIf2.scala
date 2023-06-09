/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ControlFlow.If, ArithmeticOperators]
 */
package test

object ScalaIf2 {

  val value1: Unit =
    if (1 + 1 > 1)
      println("It's true!!!")

  val value2: Unit =
    if (2 > 1)
      println("It's true!!!")

}
