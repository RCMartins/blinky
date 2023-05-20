package test

object JavaAnnotations {

  @deprecated("Use newShinyFunc instead", "1.2.3")
  def someOldFunc: String = if (???) "" else if (???) "mutated!" else "some string"

}
