import ammonite.ops._
import ammonite.ops.Shellable.StringShellable

object RunCurrentVersion {

  def run(versionNumber: String, args: Array[String]): Unit = {
    val path = pwd
    val confPath: Path = Path(args.head, base = pwd)
    val extraParams: Array[String] = args.tail

    val commitMsg = {
      val Seq(line1, line2) =
        %%("git", "log", "-2", "--pretty=format:%s")(path).out.lines.map(_.toLowerCase)

      if (line1.matches("merge [0-9a-f]{40} into [0-9a-f]{40}")) line2 else line1
    }

    val shouldDoFullTest =
      commitMsg.contains("[blinky-full]") || commitMsg.contains("[full-blinky]")
    val shouldSkipTest = commitMsg.contains("[blinky-skip]") || commitMsg.contains("[skip-blinky]")

    if (shouldSkipTest)
      println("Skipping test because commit message command")
    else {
      val allParams: Seq[String] =
        Seq(
          "launch",
          s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
          "--",
          confPath.toString,
          if (shouldDoFullTest) "--onlyMutateDiff=false" else ""
        ) ++
          extraParams.toSeq

      %.applyDynamic("cs")(allParams.filter(_.nonEmpty).map(StringShellable): _*)(path)
    }
  }
}
