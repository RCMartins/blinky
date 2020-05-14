package blinky.run

import ammonite.ops.{%, Path}

object Setup {

  def setupCoursier(path: Path): String = {
    %("curl", "-fLo", "cs", "coursier-cli-linux")(path)
    %("chmod", "+x", "cs")(path)
    "./cs"
  }

}
