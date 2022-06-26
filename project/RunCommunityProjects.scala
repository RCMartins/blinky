import os.Path

object RunCommunityProjects {

  private case class Project(url: String, folderName: String, blinkyConf: String)

  private def defaultBlinkyConf(projectName: String): String =
    s"""projectPath = "."
       |projectName = "$projectName"
       |filesToMutate = "core/src/main"
       |""".stripMargin

  private val pluginsText = """addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.4.13")"""

  private val projects: Map[String, Project] =
    Map(
      "spire" -> Project(
        "https://github.com/typelevel/spire.git",
        "spire",
        defaultBlinkyConf("testsJVM")
      )
    )

  def run(versionNumber: String, projectsToRun: Array[String]): Unit =
    projectsToRun.foreach { projectToRun =>
      projects.get(projectToRun) match {
        case None =>
          Console.err.println(s"Project not found: $projectToRun")
        case Some(project) =>
          println(s"Testing $projectToRun project:")
          testProject(versionNumber, project)
      }
    }

  private def testProject(versionNumber: String, project: Project): Unit = {
    val tempPath: Path = os.temp.dir()
    os.proc("git", "clone", project.url).call(cwd = tempPath)
    val projectPath = tempPath / project.folderName

    os.write(projectPath / ".blinky.conf", project.blinkyConf)

    os.write.append(projectPath / "project" / "plugins.sbt", pluginsText)

    os.proc(
      "cs",
      "launch",
      s"com.github.rcmartins:blinky-cli_2.13:$versionNumber",
      "--",
      "--verbose=true",
      "--dryRun=true"
    ).call(cwd = projectPath)
  }

}
