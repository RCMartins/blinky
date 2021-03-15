package test

object TermPlaceholder4 {

  def concat2(str1: String, str2: String): String = str1 + "test" + str2

  def concat3(str1: String, str2: String, str3: String): String =
    str1 + "test" + str2 + "foo" + str3

  val func1: (String, String) => Int = (_1_, _2_) => if (???) concat2(_1_, _2_).length - 5 else concat2(_1_, _2_).length + 5

  val func2: (String, String) => String = concat2(_, _) + (if (???) 1 - 1 else 1 + 1).toString

  val func3: (String, String, String) => Int = (_3_, _4_, _5_) => ///
    if (???) concat3(_3_, _4_, _5_).length - 5 else concat3(_3_, _4_, _5_).length + 5

}
