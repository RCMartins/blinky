package test

object TermPlaceholder5 {

  def isLower(c: Char): Boolean = c.isLower

  val value1 = "aBc".count(_2_ => if (???) isLower(_2_) else !isLower(_2_))

  def or321: Option[Int] => Option[Int] = _4_ => if (???) _4_ else if (???) Some(321) else _4_.orElse(Some(321))

  val value2 = or321(Some(123))

  val value3 = or321(None)

}
