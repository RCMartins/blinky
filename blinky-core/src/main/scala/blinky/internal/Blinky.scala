package blinky.internal

import java.io.{File, FileWriter}
import java.util.concurrent.atomic.AtomicInteger

import blinky.v0.BlinkyConfig
import metaconfig.Configured
import play.api.libs.json.Json
import scalafix.v1._

import scala.meta._
import scala.meta.inputs.Input.VirtualFile

class Blinky(config: BlinkyConfig) extends SemanticRule("Blinky") {
  private val mutationId: AtomicInteger = new AtomicInteger(1)
  private val mutationsPathOption: Option[File] =
    if (config.mutatorsPath.nonEmpty) Some(new File(config.mutatorsPath))
    else if (config.projectPath.nonEmpty) Some(new File(config.projectPath))
    else None

  private def nextIndex: Int = mutationId.getAndIncrement()

  def this() = this(BlinkyConfig.default)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse(name.value)(BlinkyConfig.default)
      .map(new Blinky(_))

  override def fix(implicit doc: SemanticDocument): Patch = {
    val findMutations: FindMutations = new FindMutations(config.activeMutators, doc)

    val fileName =
      doc.input match {
        case VirtualFile(path, _) => path
        case _                    => ""
      }

    def createPatch(
        mutationSeq: Seq[Mutant],
        needsParens: Boolean
    ): Option[(Patch, Seq[Mutant])] = {
      mutationSeq match {
        case Mutant(_, _, original, _, _) +: _ =>
          val (_, mutatedStr) =
            mutationSeq.map(mutation => (mutation.id, mutation.mutated)).foldRight((0, original)) {
              case ((id, mutated), (_, originalTerm)) =>
                val mutationName = Lit.String(s"SCALA_MUTATION_$id")
                val result =
                  q"""if (_root_.scala.sys.env.contains($mutationName)) ($mutated) else ($originalTerm)"""
                (0, result)
            }

          val finalSyntax = if (needsParens) "(" + mutatedStr.syntax + ")" else mutatedStr.syntax
          Some(Patch.replaceTree(original, finalSyntax), mutationSeq)
        case _ =>
          None
      }
    }

    val (finalPatch, mutantsFound): (Seq[Patch], Seq[Seq[Mutant]]) =
      findMutations
        .topTreeMutations(doc.tree)
        .flatMap {
          case (term, MutatedTerms(mutationsFound, needsParens)) =>
            val mutationSeq =
              mutationsFound.map(mutated => createMutant(term, mutated, fileName))
            createPatch(mutationSeq, needsParens = needsParens)
        }
        .unzip

    saveNewMutantsToFile(mutantsFound.flatten)
    finalPatch.asPatch
  }

  def createMutant(original: Term, mutated: Term, fileName: String): Mutant = {
    val mutantIndex = nextIndex
    val pos = original.pos
    val input = pos.input.text

    val startDiffBefore = pos.start - pos.startColumn
    val endDiffBefore = {
      val p = input.indexOf("\n", pos.end)
      if (p == -1) input.length else p
    }
    val mutatedInput = input.substring(0, pos.start) + mutated.syntax + input.substring(pos.end)
    val startDiffAfter = startDiffBefore
    val endDiffAfter = {
      val p = mutatedInput.indexOf("\n", pos.start + mutated.syntax.length)
      if (p == -1) mutatedInput.length else p
    }

    def addLineNumbers(
        startLine: Int,
        linesBefore: List[String],
        linesAfter: List[String]
    ): List[String] = {
      val fileDiffSize = 1 + Math
        .log10(startLine + Math.max(linesBefore.size, linesAfter.size))
        .toInt
      linesBefore.zipWithIndex.map {
        case (line, index) => ("%" + fileDiffSize + "d: %s").format(startLine + index + 1, line)
      } ++
        linesAfter.zipWithIndex.map {
          case (line, index) => ("%" + fileDiffSize + "d: %s").format(startLine + index + 1, line)
        }
    }

    val diffLines: List[String] =
      fileName +:
        addLineNumbers(
          pos.startLine,
          input.substring(startDiffBefore, endDiffBefore).split("\n").toList.map("-" + _),
          mutatedInput.substring(startDiffAfter, endDiffAfter).split("\n").toList.map("+" + _)
        )

    Mutant(mutantIndex, diffLines, original, mutated)
  }

  def saveNewMutantsToFile(mutantsFound: Seq[Mutant]): Unit = {
    mutationsPathOption.foreach { mutatorsPath =>
      if (mutantsFound.nonEmpty) {
        val jsonMutationReport = mutantsFound.map(Json.toJson(_)).mkString("", "\n", "\n")
        new FileWriter(new File(mutatorsPath, "mutations.json"), true) {
          write(jsonMutationReport)
          close()
        }
      }
    }
  }
}
