package blinky.internal

import java.util.concurrent.atomic.AtomicInteger

import ammonite.ops._
import better.files.File
import blinky.internal.MutatedTerms._
import blinky.v0.BlinkyConfig
import metaconfig.Configured
import play.api.libs.json.Json
import scalafix.v1._

import scala.meta._
import scala.meta.inputs.Input.VirtualFile
import scala.util.Try

class Blinky(config: BlinkyConfig) extends SemanticRule("Blinky") {
  private val mutantId: AtomicInteger = new AtomicInteger(1)
  private val tempVarId: AtomicInteger = new AtomicInteger(1)
  private val mutantsOutputFileOpt: Option[File] =
    Some(config.mutantsOutputFile).filter(_.nonEmpty).map(File(_))
  mutantsOutputFileOpt.foreach(_.createFileIfNotExists())

  private def nextMutantIndex: Int = mutantId.getAndIncrement()
  private def nextTempVarIndex: Int = tempVarId.getAndIncrement()

  private val fileShouldBeMutated: String => Boolean =
    if (config.filesToMutate == Seq("all"))
      (_: String) => true
    else
      config.filesToMutate.toSet

  def this() = this(BlinkyConfig.default)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse(name.value)(BlinkyConfig.default)
      .map(new Blinky(_))

  override def fix(implicit doc: SemanticDocument): Patch = {
    val findMutations: FindMutations = new FindMutations(config.activeMutators, doc)

    val VirtualFile(fileName, _) = doc.input

    if (!fileShouldBeMutated(fileName))
      Patch.empty
    else {
      def createPatch(
          original: Term,
          mutantSeq: Seq[Mutant],
          placeholderFunction: Term => Term,
          replaceTempVars: Seq[(String, Term)],
          needsParens: Boolean
      ): Option[(Patch, Seq[Mutant])] =
        mutantSeq.headOption.map(_.original).map { originalReplaced =>
          def replacer(initialTerm: Term): Term =
            replaceTempVars
              .foldLeft(initialTerm: Tree) { case (updatedTerm, (from, to)) =>
                updatedTerm.transform { case Term.Name(`from`) => to }
              }
              .asInstanceOf[Term]

          val (_, mutatedStr) =
            mutantSeq.map(mutant => (mutant.id, mutant.mutated)).foldRight((0, originalReplaced)) {
              case ((id, mutatedTerm), (_, originalTerm)) =>
                val mutantId = Lit.String(s"BLINKY_MUTATION_$id")
                val result =
                  q"""if (_root_.scala.sys.env.contains($mutantId)) ($mutatedTerm) else ($originalTerm)"""
                (0, replacer(placeholderFunction(result)))
            }

          println("/" * 40)
          println(original)
          println(originalReplaced)
          println(syntaxParens(mutatedStr, needsParens))
          println("\\" * 40)

          (Patch.replaceTree(original, syntaxParens(mutatedStr, needsParens)), mutantSeq)
        }

      val (finalPatch, mutantsFound): (Seq[Patch], Seq[Seq[Mutant]]) =
        findMutations
          .topTreeMutations(doc.tree)
          .flatMap {
            case (original, StandardMutatedTerms(mutantsFound, needsParens)) =>
              val mutantsSeq =
                mutantsFound
                  .filterNot(_.structure == original.structure)
                  .map(mutated =>
                    createMutant(original, original, mutated, mutated, needsParens, fileName)
                  )
              createPatch(original, mutantsSeq, identity, Seq.empty, needsParens = needsParens)
            case (
                  originalWithP,
                  PlaceholderMutatedTerms(
                    originalWithoutP,
                    placeholderFunction,
                    mutationsFound,
                    newVars,
                    needsParens
                  )
                ) =>
              println(
                s"""***************
                   |$originalWithP
                   |$originalWithoutP
                   |$placeholderFunction
                   |$mutationsFound
                   |$needsParens
                   |***************
                   |""".stripMargin
              )

              val mutantSeq: Seq[Mutant] =
                mutationsFound
                  .filterNot { case (termWithP, _) =>
                    termWithP.structure == originalWithP.structure
                  }
                  .map { case (termWithP, termWithoutP) =>
                    createMutant(
                      originalWithoutP,
                      originalWithP,
                      termWithoutP,
                      termWithP,
                      needsParens,
                      fileName
                    )
                  }

              val tempVarsReplaces =
                newVars.map {
                  (_, Term.Name(s"_BLINKY_TEMP_$nextTempVarIndex"))
                }

              createPatch(
                originalWithP,
                mutantSeq,
                placeholderFunction,
                tempVarsReplaces,
                needsParens = needsParens
              )
          }
          .unzip

      saveNewMutantsToFile(mutantsFound.flatten)
      finalPatch.asPatch
    }
  }

  def createMutant(
      original: Term,
      originalForDiff: Term,
      mutated: Term,
      mutatedForDiff: Term,
      needsParens: Boolean,
      fileName: String
  ): Mutant = {
    val pos = originalForDiff.pos
    val input = pos.input.text

    val mutatedSyntax = syntaxParens(mutatedForDiff, needsParens)
    val mutatedInput = input.substring(0, pos.start) + mutatedSyntax + input.substring(pos.end)

    val gitDiff: String = calculateGitDiff(originalForDiff, mutatedInput)
    // mutatedForDiff?
    Mutant(
      nextMutantIndex,
      gitDiff,
      fileName,
      original,
      originalForDiff,
      mutated,
      mutatedForDiff,
      needsParens
    )
  }

  def calculateGitDiff(original: Term, mutatedInput: String): String =
    mutantsOutputFileOpt match {
      case None =>
        ""
      case Some(_) =>
        File.temporaryFile() { originalFile =>
          originalFile.writeText(original.pos.input.text)

          File.temporaryFile() { mutatedFile =>
            mutatedFile.writeText(mutatedInput)

            val gitDiff =
              Try(
                %%(
                  'git,
                  'diff,
                  "--no-index",
                  originalFile.toString,
                  mutatedFile.toString
                )(pwd)
              ).failed.get.toString
                .split("\n")
                .drop(5)
                .mkString("\n")

            println("=" * 40)
            println("DIFF")
            println(gitDiff)
            println("=" * 40)

            gitDiff
          }
        }
    }

  def saveNewMutantsToFile(mutantsFound: Seq[Mutant]): Unit =
    if (mutantsFound.nonEmpty)
      mutantsOutputFileOpt.foreach { mutantsOutputFile =>
        val jsonMutationReport = mutantsFound.map(Json.toJson(_)).map(_.toString)
        mutantsOutputFile.appendLines(jsonMutationReport: _*)
      }

}
