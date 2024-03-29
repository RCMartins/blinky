/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = LiteralStrings
 */
package test

object JavaAnnotations {

  @deprecated("Use newDef instead", "1.2.3")
  def someDef: String = "some string"

  @deprecated("Use newObject instead", "1.2.3")
  object SomeObject

  @deprecated("Use newTrait instead", "1.2.3")
  trait SomeTrait

  @deprecated("Use newClass instead", "1.2.3")
  class SomeClass

  @deprecated("Use newCaseClass instead", "1.2.3")
  case class CaseClass()

  @deprecated("Use newType instead", "1.2.3")
  type SomeType = String

}
