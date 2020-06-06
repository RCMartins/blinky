package blinky.runZIO.config

case class Args(
    mainConfFile: Option[String] = None,
    overrides: Seq[MutationsConfig => MutationsConfig] = Seq.empty
) {

  def add(over: MutationsConfig => MutationsConfig): Args =
    copy(overrides = overrides :+ over)

}
