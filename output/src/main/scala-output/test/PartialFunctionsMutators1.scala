package test

object PartialFunctionsMutators1 {
  val value1 =
    List(10, 20, 30).map {
      (if (???) {
  case 20 => false
  case 30 => true
  case _ => false
} else if (???) {
  case 10 => true
  case 30 => true
  case _ => false
} else if (???) {
  case 10 => true
  case 20 => false
  case _ => false
} else if (???) {
  case 10 => true
  case 20 => false
  case 30 => true
} else {
  case 10 => true
  case 20 => false
  case 30 => true
  case _ => false
})
    }

  val value2 =
    List(10, 20).map { case _ =>
      false
    }

  val value3 =
    List(10, 20).map {
      (if (???) {
  case _ =>
    1 + 1
} else if (???) {
  case 10 => 5
} else if (???) {
  case 10 =>
    5
  case _ =>
    1 - 1
} else {
  case 10 =>
    5
  case _ =>
    1 + 1
})
    }
}