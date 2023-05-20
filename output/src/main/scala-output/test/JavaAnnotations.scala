package test

object JavaAnnotations {

  @deprecated("Use newDef instead", "1.2.3")
  def someDef: String = if (???) "" else if (???) "mutated!" else "some string"

  @deprecated("Use newObject instead", "1.2.3")
  object SomeObject

  @deprecated("Use newClass instead", "1.2.3")
  class SomeClass

}
