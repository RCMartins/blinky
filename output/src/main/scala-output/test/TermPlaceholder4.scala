package test

object TermPlaceholder4 {

  def concat2(str1: String, str2: String): String = str1 + "test" + str2

  def concat3(str1: String, str2: String, str3: String): String =
    str1 + "test" + str2 + "foo" + str3

  val func1: (String, String) => Int = (_5_, _6_) => if (???) concat2(_5_, _6_).length - 5 else concat2(_5_, _6_).length + 5

  val func2: (String, String) => String = (_7_, _8_) => if (???) concat2(_7_, _8_) + (1 - 1).toString else concat2(_7_, _8_) + (1 + 1).toString

  val func3: (String, String, String) => Int = (_15_, _16_, _17_) => ///
    if (???) concat3(_15_, _16_, _17_).length - 5 else concat3(_15_, _16_, _17_).length + 5

}
