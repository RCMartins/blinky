import $file.utils, utils._

import ammonite.ops._
import ammonite.ops.Shellable.StringShellable

@main
def main(confPath: Path, extraParams: String*): Unit = {
  val path = pwd
  val versionNumber = publishLocalBlinky()

  val shouldDoFullTest =
    sys.env.get("TRAVIS_COMMIT_MESSAGE").exists(_.contains("[full-ci]"))

  val allParams: Seq[String] =
    Seq(
      "launch",
      s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
      "--",
      confPath.toString,
      if (shouldDoFullTest) "onlyMutateDiff=false" else ""
    ) ++
      extraParams.toSeq

  %.applyDynamic("cs")(allParams.filter(_.nonEmpty).map(StringShellable): _*)(path)
}
