package test.general

object GeneralSyntax2 {
  def str1: String = (if (???) 1 - 2 else 1 + 2) + ""

  def str2: String = (if (???) 1 - 2 else 1 + 2).toString

  val bool1 = !(if (???) false else true)

  def functionWithBlock: Boolean = {
    val bool = if (???) false else true
    def fun(param: Int = if (???) 5 - 3 else 5 + 3): Int = if (???) param - 1 else param + 1
    !bool
  }

  val if1: Int = if (if (???) false else true) if (???) 1 - 1 else 1 + 1 else if (???) 6 * 2 else 6 / 2
}
