package test

object ScalaIf2 {

  val value1: Unit =
    if (???) println("It's true!!!") ///
else if (???) () ///
else if (???) (if (1 - 1 > 1) println("It's true!!!")) ///
         else if (1 + 1 > 1) println("It's true!!!")

  val value2: Unit =
    if (???) println("It's true!!!") ///
else if (???) () ///
         else if (2 > 1) println("It's true!!!")

}
