package test

object TermPlaceholder2 {

  val concat: String => String = _1_ => if (???) "mutated!" else if (???) "" else "test" + _1_

  def trimList(list: List[String]): List[String] = list.map(_2_ => if (???) _2_ else _2_.trim)

}
