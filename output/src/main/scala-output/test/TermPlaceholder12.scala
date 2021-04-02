package test

object TermPlaceholder12 {

  implicit class StringExtensions(initial: String) {
    def rep(str1: String, str2: String): String = initial.replace(str1, str2)
  }

  def result1: String => String = _1_ => (if (???) _1_ else _1_.trim).rep("abc", "def")

  def result2: (String, String) => String = (_5_, _6_) => if (???) _5_.rep(_6_, "def") else _5_.trim.rep(_6_, "def")

  def result3: (String, String) => String = (_10_, _11_) => if (???) _10_.rep("abc", _11_) else _10_.trim.rep("abc", _11_)

}
