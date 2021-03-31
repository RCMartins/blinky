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

  def result: String => String = _4_ => (if (???) _4_ else _4_.trim) ! ("abc", "def")

}
