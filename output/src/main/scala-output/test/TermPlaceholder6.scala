package test

object TermPlaceholder6 {

  def or321mapped: Option[Int] => Option[Int] = _1_ => (if (???) _1_ else if (???) Some(321) else _1_.orElse(Some(321))).map(n => n * 2)

  def or321mapped2: Option[Int] => Option[Int] = _2_ => (if (???) _2_ else if (???) Some(321) else _2_.orElse(Some(321))).map(_3_ => if (???) _3_ - 1 else _3_ + 1)

  def or321mapped3: Option[String] => Option[Int] = _4_ => (if (???) _4_ else if (???) Some("abc") else _4_.orElse(Some("abc"))).map(_5_ => (if (???) _5_ else _5_.trim).hashCode())

}
