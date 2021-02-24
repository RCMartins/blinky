package test

object TermPlaceholder1 {

  def concat(str: String): String = str + "test"

  val func1: String => Int = _BLINKY_TEMP_1 => if (???) concat(_BLINKY_TEMP_1).length - 5 else concat(_BLINKY_TEMP_1).length + 5

  //convert only the right side
  val func2: String => String = concat(_) + (if (???) 1 - 1 else 1 + 1).toString

}
