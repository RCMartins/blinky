package test

object TermPlaceholder4 {

  def concat2(str1: String, str2: String): String = str1 + "test" + str2

  def concat3(str1: String, str2: String, str3: String): String =
    str1 + "test" + str2 + "foo" + str3

  val func1: (String, String) => Int = (_5_, _6_) => if (???) concat2(_5_, _6_).length - 5 else concat2(_5_, _6_).length + 5

  val func2: (String, String) => String = concat2(_, _) + (if (???) 1 - 1 else 1 + 1).toString

  val func3: (String, String, String) => Int = (_13_, _14_, _15_) => ///
    if (???) concat3(_13_, _14_, _15_).length - 5 else concat3(_13_, _14_, _15_).length + 5

}
