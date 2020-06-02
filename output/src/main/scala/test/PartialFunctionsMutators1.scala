package test

object PartialFunctionsMutators1 {
  val value1 =
    List(10, 20, 30).map (if (???) {
  case 10 => true
  case 20 => false
  case 30 => true
} else if (???) {
  case 10 => true
  case 20 => false
  case _ => false
} else if (???) {
  case 10 => true
  case 30 => true
  case _ => false
} else if (???) {
  case 20 => false
  case 30 => true
  case _ => false
} else {
  case 10 => true
  case 20 => false
  case 30 => true
  case _ => false
})

  val value2 =
    List(10, 20).map {
      case _ => false
    }
}
