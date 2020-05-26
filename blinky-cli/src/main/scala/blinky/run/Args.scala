package blinky.run

import better.files.File

case class Args(
    mainConfFile: Option[File] = None,
    overrides: Seq[MutationsConfig => MutationsConfig] = Seq.empty
) {

  def add(over: MutationsConfig => MutationsConfig): Args =
    copy(overrides = overrides :+ over)

}
