import os.{Path, ProcessOutput}

object RunCommunityProjects {

  private case class Project(
      url: String,
      folderName: String,
      blinkyConf: String,
      hash: Option[String],
      extraCommands: Path => Unit = _ => ()
  )

  private def blinkyConf(projectName: String, filesToMutate: String): String =
    s"""projectPath = "."
       |projectName = "$projectName"
       |filesToMutate = "$filesToMutate"
       |""".stripMargin

//  private val sbtBloopVersion = "1.5.6-213-2e0eb7f1-SNAPSHOT"
//  private val sbtBloopVersion = "1.5.6-63-b0854615"
  private val sbtBloopVersion = "1.5.6"
//  private val sbtBloopVersion = "1.4.3"

  private val pluginsText = s"""addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "$sbtBloopVersion")"""

  private val projects: Map[String, Project] =
    Map(
      "spire" -> Project(
        "https://github.com/typelevel/spire.git",
        "spire",
        blinkyConf("testsJVM", "core/src/main"),
//        Some("c13c708cb6e8a42935a8a1543ad0d29a6b2d0fb0"),
        // Some("0318050dbe81642528c0dbb2e59fd6c6d361ae18"),
        None,
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

    project.extraCommands(projectPath)
    project.hash.foreach(hash => os.proc("git", "checkout", hash).call(cwd = projectPath))

    os.write(projectPath / ".blinky.conf", project.blinkyConf)

    os.write.append(projectPath / "project" / "plugins.sbt", pluginsText)

    os.proc(
      "cs",
      "launch",
      s"com.github.rcmartins:blinky-cli_2.13:$versionNumber",
      "--",
      "--verbose=true",
      "--dryRun=true"
    ).call(
      cwd = projectPath,
      stdout = ProcessOutput.Readlines(println),
      stderr = ProcessOutput.Readlines(Console.err.println)
    )
  }

}
