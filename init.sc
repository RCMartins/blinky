import ammonite.ops.ImplicitWd._
import ammonite.ops._

// curl -Lo coursier https://git.io/coursier-cli &&
//    chmod +x coursier &&
//    ./coursier --help
%curl("-Lo", "coursier", "https://git.io/coursier-cli")
%chmod("+x", "coursier")
%("./coursier", "--help")

// coursier bootstrap ch.epfl.scala:scalafix-cli_2.12.8:0.9.5 -f --main scalafix.cli.Cli -o scalafix
%coursier('bootstrap, "ch.epfl.scala:scalafix-cli_2.12.8:0.9.5", "-f", "--main", "scalafix.cli.Cli", "-o", "scalafix")

// ./scalafix --version # Should say 0.9.5
%("./scalafix", "--version")
