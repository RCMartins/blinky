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

  def result2: (String, String) => String = (_8_, _9_) => if (???) _8_ ! (_9_, "def") else _8_.trim ! (_9_, "def")

  def result3: (String, String) => String = (_14_, _15_) => if (???) _14_ ! ("abc", _15_) else _14_.trim ! ("abc", _15_)

}
