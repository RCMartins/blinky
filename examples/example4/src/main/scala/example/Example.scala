import Utils.Number

object Example {
  def calc(nOpt: Option[Int]): String = {
    nOpt
      .map { n =>
        if (n > Number)
          "big"
        else
          "small"
      }
      .getOrElse("<empty>")
  }
}
