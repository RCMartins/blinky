import ammonite.ops._
import os.copy

object RunCommunityProjects {

  private case class Project(url: String, folderName: String, blinkyConf: String)

  private def defaultBlinkyConf(projectName: String, filesToMutate: String): String =
    s"""projectPath = "."
       |projectName = "$projectName"
       |filesToMutate = "$filesToMutate"
       |""".stripMargin

  private val pluginsText = """addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.4.8")"""

  private val projects: Map[String, Project] =
    Map(
      "spire" -> Project(
        "https://github.com/typelevel/spire.git",
        "spire",
        defaultBlinkyConf("testsJVM", "core/src/main")
      ),
      "playframework" -> Project(
        "https://github.com/playframework/playframework.git",
        "playframework",
        defaultBlinkyConf("Play", "core/play/src/main")
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
    val tempPath: Path = tmp.dir()
    %("git", "clone", project.url)(tempPath)
    val projectPath = tempPath / project.folderName

    write(projectPath / ".blinky.conf", project.blinkyConf)

    write.append(projectPath / "project" / "plugins.sbt", pluginsText)

    %(
      "cs",
      "launch",
      s"com.github.rcmartins:blinky-cli_2.12:$versionNumber",
      "--",
      "--verbose=true",
      "--dryRun=true"
    )(projectPath)
  }

}
