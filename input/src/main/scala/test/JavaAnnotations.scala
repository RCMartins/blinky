/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = LiteralStrings
 */
package test

object JavaAnnotations {

  @deprecated("Use newShinyFunc instead", "1.2.3")
  def someOldFunc: String = "some string"

}
