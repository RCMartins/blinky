import $file.utils, utils._

import ammonite.ops._
import ammonite.ops.Shellable.StringShellable

@main
def main(confPath: Path, extraParams: String*): Unit = {
  val path = pwd
  val versionNumber = publishLocalBlinky()

  val shouldDoFullTest = {
    val commitHash = sys.env("TRAVIS_COMMIT")
    %%("git", "log", "-1", "--pretty=format:%s")(path).out.string.toLowerCase.contains("[full-ci]")
  }

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
