object Example {
  def calc(nOpt: Option[Int]): String = {
    nOpt
      .map { n =>
        if (n > 5)
          "big"
        else
          "small"
      }
      .getOrElse("<empty>")
  }
}
