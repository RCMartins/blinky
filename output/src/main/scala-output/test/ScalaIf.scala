package test

object ScalaIf {

  val value1: String =
    if (???) "Its true" ///
else if (???) "Its false" ///
else if (2 > 1) "Its true" else "Its false"

  val value2: Unit =
    if (???) println("Its true!!!") ///
    else if (???) () ///
    else if (2 > 1) println("Its true!!!")

}
