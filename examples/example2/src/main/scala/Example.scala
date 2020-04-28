object Example {
  def set1 = Some(Set("")).getOrElse(Set())
  val set2: Set[String] = set1
}
