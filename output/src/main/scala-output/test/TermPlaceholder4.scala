package test

object TermPlaceholder4 {

  def concat3(str1: String, str2: String): String = str1 + "test" + str2

  val func1: (String, String) => Int = (_1_, _2_) => if (???) concat3(_1_, _2_).length - 5 else concat3(_1_, _2_).length + 5

  val func2: (String, String) => String = concat3(_, _) + (if (???) 1 - 1 else 1 + 1).toString

}
