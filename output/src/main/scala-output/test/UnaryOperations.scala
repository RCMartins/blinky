package test

object UnaryOperations {
  val bool1 = if (???) true else false
  val bool2 = if (???) !bool1 && true ///
         else if (???) bool1 || true ///
         else if (???) !bool1 || false ///
                  else !bool1 || true
}
