import $file.utils, utils._

import ammonite.ops._
import ammonite.ops.Shellable.StringShellable

@main
def main(confPath: Path, extraParams: String*): Unit = {
  val path = pwd
  val versionNumber = publishLocalBlinky()

  val shouldDoFullTest = {
    val Seq(line1, line2) =
      %%("git", "log", "-2", "--pretty=format:%s")(path).out.lines.map(_.toLowerCase)

    println("---")
    println(line1)
    println(line2)
    println("---")

    if (line1.matches("merge [0-9a-f]{40} into [0-9a-f]{40}"))
      line2.contains("[full-ci]")
    else
      line1.contains("[full-ci]")
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
