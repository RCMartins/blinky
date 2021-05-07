package test

object TermPlaceholder11 {

  def useF(
      f: List[String] => List[String],
      list: List[String]
  ): List[String] = f(list)

  def value: List[String] => List[String] = ///
    _3_ => if (???) useF(_.reverse, _3_) ///
      else if (???) useF(identity, _3_).reverse ///
               else useF(_.reverse, _3_).reverse

  implicit class StringExtensions(initial: String) {
    def !(str1: String, str2: String): String = initial.replace(str1, str2)
  }

  def result1: String => String = _4_ => (if (???) _4_ else _4_.trim) ! ("abc", "def")

  def result2: (String, String) => String = (_7_, _8_) => if (???) _7_ ! (_8_, "def") else _7_.trim ! (_8_, "def")

  def result3: (String, String) => String = (_13_, _14_) => if (???) _13_ ! ("abc", _14_) else _13_.trim ! ("abc", _14_)

}
