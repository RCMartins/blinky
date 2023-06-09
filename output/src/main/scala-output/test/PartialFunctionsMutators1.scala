package test

object PartialFunctionsMutators1 {
  val value1 =
    List(10, 20, 30).map (if (???) {
  case 10 if false => true
  case 20 => false
  case 30 => true
  case _ => false
} else if (???) {
  case 10 => true
  case 20 if false => false
  case 30 => true
  case _ => false
} else if (???) {
  case 10 => true
  case 20 => false
  case 30 if false => true
  case _ => false
} else if (???) {
  case 10 => true
  case 20 => false
  case 30 => true
  case _ if false => false
} else {
  case 10 => true
  case 20 => false
  case 30 => true
  case _ => false
})

  val value2 =
    List(10, 20).map { case _ =>
      false
    }

  val value3 =
    List(10, 20).map (if (???) {
  case 10 if false =>
    5
  case _ =>
    1 + 1
} else if (???) {
  case 10 =>
    5
  case _ if false =>
    1 + 1
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
