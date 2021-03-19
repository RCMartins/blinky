package test

object TermPlaceholder5 {

  def isLower(c: Char): Boolean = c.isLower

  val value1 = "aBc".count(_1_ => if (???) isLower(_1_) else !isLower(_1_))

  def or321: Option[Int] => Option[Int] = _2_ => if (???) _2_ else if (???) Some(321) else _2_.orElse(Some(321))

  val value2 = or321(Some(123))

  val value3 = or321(None)

}
