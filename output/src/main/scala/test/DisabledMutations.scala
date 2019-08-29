package test

object DisabledMutations {

  val v1 = true
  val v2 = false
  val v3 = 100 * 5
  val v4 = if (???) v1 && v2 else v1 || v2
  val v5 = if (???) Some(100) ///
      else if (???) Some(100).filterNot(value => value > 50) ///
               else Some(100).filter(value => value > 50)
  val v6 = v5.orElse(Some(50))

}
