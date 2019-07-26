package test

object ConditionalExpressions1 {

  val bool1 = true
  val bool2 = false
  val bool3 = if (???) bool1 || bool2 else bool1 && bool2
  val bool4 = if (???) bool3 && bool2 else bool3 || bool2
  val bool5 = if (???) bool4 else !bool4

}
