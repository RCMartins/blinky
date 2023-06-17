import os.{Path, ProcessOutput}

object RunCommunityProjects {

  private val Bloop = "bloop"
  private val SBT = "sbt"

  private val sbtBloopVersion = "1.5.6"
  private val pluginsText = s"""addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "$sbtBloopVersion")"""

  private val projects: Map[String, Project] =
    Map(
      "scalameta" ->
        Project(
          url = "https://github.com/scalameta/scalameta.git",
          folderName = "scalameta",
          projectName = "",
          filesToMutate = "scalameta",
          compileCommand = "testsJVM/compile",
          testCommand = "testsJVM/test",
          hash = Some("ccd351c4bb681e1bdf44e3a9f0c789e0158be7f3"),
          testRunner = SBT,
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
    project.hash.foreach(hash => os.proc("git", "checkout", hash).call(cwd = projectPath))
    project.extraCommands(projectPath)

    os.write(projectPath / ".blinky.conf", blinkyConf(project))

    if (project.testRunner == Bloop)
      os.write.append(projectPath / "project" / "plugins.sbt", pluginsText)

    os.proc(
      "cs",
      "launch",
      s"com.github.rcmartins:blinky-cli_2.13:$versionNumber",
    ).call(
      cwd = projectPath,
      stdout = ProcessOutput.Readlines(println),
      stderr = ProcessOutput.Readlines(Console.err.println)
    )
  }

  private final case class Project(
      url: String,
      folderName: String,
      projectName: String,
      filesToMutate: String,
      compileCommand: String,
      testCommand: String,
      hash: Option[String],
      extraCommands: Path => Unit = _ => (),
      testRunner: String = SBT,
  )

  private def blinkyConf(project: Project): String =
    s"""projectPath = "."
       |projectName = "${project.projectName}"
       |filesToMutate = "${project.filesToMutate}"
       |options.testRunner = "${project.testRunner}"
       |options.compileCommand = "${project.compileCommand}"
       |options.testCommand = "${project.testCommand}"
       |options.dryRun = true
       |""".stripMargin

}
