package test

object TermPlaceholder6 {

  def or321mapped: Option[Int] => Option[Int] = _2_ => (if (???) _2_ else if (???) Some(321) else _2_.orElse(Some(321))).map(n => n * 2)

  def or321mapped2: Option[Int] => Option[Int] = _4_ => (if (???) _4_ else if (???) Some(321) else _4_.orElse(Some(321))).map(_5_ => if (???) _5_ - 1 else _5_ + 1)

  def or321mapped3: Option[String] => Option[Int] = _7_ => (if (???) _7_ else if (???) Some("abc") else _7_.orElse(Some("abc"))).map(_8_ => (if (???) _8_ else _8_.trim).hashCode())

}
