import ammonite.ops._

object RunCommunityProjects {

  private case class Project(
      url: String,
      folderName: String,
      blinkyConf: String,
      hash: Option[String],
      extraCommands: Path => Unit
  )

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
        defaultBlinkyConf("testsJVM", "core/src/main"),
        Some("c13c708cb6e8a42935a8a1543ad0d29a6b2d0fb0"),
        _ => ()
      ),
      "playframework" -> Project(
        "https://github.com/playframework/playframework.git",
        "playframework",
        defaultBlinkyConf("Play", "core/play/src/main"),
        None,
        path => %("git", "fetch", "--unshallow")(path)
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

    project.extraCommands(projectPath)
    project.hash.foreach(hash => %("git", "checkout", hash)(projectPath))

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
