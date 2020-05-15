object Example {
  def set1: Set[String] /* issue if this type is not explicitly set - SLS 3.2.10 */ = Some(Set("")).getOrElse(Set())
  val set2: Set[String] = set1
}
