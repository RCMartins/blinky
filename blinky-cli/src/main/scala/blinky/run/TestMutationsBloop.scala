//package blinky.run
//
//import ammonite.ops.{CommandResult, Path}
//import blinky.run.ExternalCalls._
//import blinky.run.Utils._
//import blinky.v0.BlinkyConfig
//import play.api.libs.json.Json
//
//import scala.util.{Failure, Random, Success, Try}
//
//object TestMutationsBloop {
//  def run(
//      projectPath: Path,
//      blinkyConfig: BlinkyConfig,
//      options: OptionsConfig
//  ): Boolean = {
//
//    val mutationReport: List[Mutant] =
//      readFile(Path(blinkyConfig.mutantsOutputFile))
//        .split("\n")
//        .filter(_.nonEmpty)
//        .map(Json.parse(_).as[Mutant])
//        .toList
//    val testCommand = options.testCommand
//    val numberOfMutants = mutationReport.length
//
//    def runMutationsSetup(originalTestTime: Long): Boolean = {
//      val mutationsToTest =
//        if (originalTestTime * mutationReport.size >= options.maxRunningTime.toMillis)
//          Random.shuffle(mutationReport)
//        else
//          mutationReport
//
//      println(
//        s"Running the same tests on mutated code (maximum of ${options.maxRunningTime})"
//      )
//
//      val initialTime = System.currentTimeMillis()
//      val results = runMutations(mutationsToTest, initialTime)
//      val totalTime = System.currentTimeMillis() - initialTime
//
//      ConsoleReporter.reportMutationResult(results, totalTime, numberOfMutants, options)
//    }
//
//    def runMutations(mutants: List[Mutant], initialTime: Long): List[(Int, Boolean)] =
//      mutants match {
//        case Nil =>
//          Nil
//        case _ if System.currentTimeMillis() - initialTime > options.maxRunningTime.toMillis =>
//          println(
//            s"Timed out - maximum of ${options.maxRunningTime} " +
//              s"(this can be changed with --maxRunningTime parameter)"
//          )
//          Nil
//        case mutant :: othersMutants =>
//          val id = mutant.id
//          val time = System.currentTimeMillis()
//          val testResult = runInBloop(mutant)
//
//          val result =
//            if (testResult.isSuccess) {
//              println(red(s"Mutant #$id was not killed!"))
//              if (!options.verbose)
//                println(
//                  prettyDiff(mutant.diff, mutant.fileName, projectPath.toString, color = true)
//                )
//              id -> false
//            } else {
//              println(green(s"Mutant #$id was killed."))
//              id -> true
//            }
//          if (options.verbose) {
//            println(s"time: ${System.currentTimeMillis() - time}")
//            println("-" * 40)
//          }
//
//          result :: runMutations(othersMutants, initialTime)
//      }
//
//    def runInBloop(mutant: Mutant): Try[CommandResult] = {
//      if (options.verbose) {
//        println(
//          s"""> [BLINKY_MUTATION_${mutant.id}=1] bash -c "bloop test ${escapeString(
//            testCommand
//          )}""""
//        )
//        println(prettyDiff(mutant.diff, mutant.fileName, projectPath.toString, color = true))
//        println("--v--" * 5)
//      }
//
//      Try(
//        runBash(
//          Seq(
//            "bloop",
//            "test",
//            escapeString(testCommand)
//          ),
//          Map(s"BLINKY_MUTATION_${mutant.id}" -> "1")
//        )(projectPath)
//      )
//    }
//
//    println(s"$numberOfMutants mutants found.")
//    if (numberOfMutants == 0) {
//      println("Try changing the mutation settings.")
//      true
//    } else {
//      runSync("sbt", Seq("bloopInstall"))(projectPath)
//      println("Running tests with original config")
//      val compileResult =
//        Try(runBash(Seq("bloop", "compile", escapeString(options.compileCommand)))(projectPath))
//      compileResult match {
//        case Failure(error) =>
//          val newIssueLink = "https://github.com/RCMartins/blinky/issues/new"
//          Console.err.println(error)
//          Console.err.println(
//            s"""There are compile errors after applying the Blinky rule.
//               |This could be because Blinky is not configured correctly.
//               |Make sure compileCommand is set.
//               |If you think it's due to a bug in Blinky please to report in:
//               |$newIssueLink""".stripMargin
//          )
//          false
//        case Success(_) =>
//          val originalTestInitialTime = System.currentTimeMillis()
//          val vanillaResult =
//            Try(runBash(Seq(s"bloop test ${escapeString(testCommand)}"))(projectPath))
//          vanillaResult match {
//            case Failure(error) =>
//              Console.err.println(
//                s"""Tests failed. No mutations will run until this is fixed.
//                   |This could be because Blinky is not configured correctly.
//                   |Make sure testCommand is set.
//                   |
//                   |$error
//                   |""".stripMargin
//              )
//              false
//            case Success(result) =>
//              println(green("Original tests passed..."))
//              if (options.verbose)
//                println(result.out.string)
//              val originalTestTime = System.currentTimeMillis() - originalTestInitialTime
//              if (options.verbose)
//                println(green("time: " + originalTestTime))
//              if (options.dryRun) {
//                println(
//                  s"""
//                     |${green("In dryRun mode. Everything worked correctly.")}
//                     |If you want to run it again with mutations active use --dryRun=false
//                     |""".stripMargin
//                )
//                true
//              } else
//                runMutationsSetup(originalTestTime)
//          }
//      }
//    }
//  }
//
//}
