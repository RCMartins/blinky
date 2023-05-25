/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ControlFlow.If]
 */
package test

object ScalaIf1 {

  val value1: String =
    if (2 > 1)
      "Its true"
    else
      "Its false"

}
