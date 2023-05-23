/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ControlFlow.If, ArithmeticOperators]
 */
package test

object ScalaIf2 {

  // Because of a bug in scalameta this expressions can not be mutated safely
  // https://github.com/scalameta/scalameta/issues/3128

  val value1: Unit =
    if (1 + 1 > 1)
      println("It's true!!!")

  val value2: Unit =
    if (2 > 1)
      println("Its true!!!")

}
