/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ControlFlow.If]
 */
package test

object ScalaIf {

  val value1: String =
    if (2 > 1)
      "Its true"
    else
      "Its false"

  val value2: Unit =
    if (2 > 1)
      println("Its true!!!")

}
