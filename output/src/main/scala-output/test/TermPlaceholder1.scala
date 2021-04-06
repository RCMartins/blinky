package test

object TermPlaceholder1 {

  def concat(str: String): String = str + "test"

  val func1: String => Int = _3_ => if (???) concat(_3_).length - 5 else concat(_3_).length + 5

  val func2: String => String = _4_ => if (???) concat(_4_) + (1 - 1).toString else concat(_4_) + (1 + 1).toString

}
