object Example {
  def calc(nOpt: Option[Int]): String =
    if (false)
      calc(nOpt.map(_ + 1))
    else
      "result"
}
