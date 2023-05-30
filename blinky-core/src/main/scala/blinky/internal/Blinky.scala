package blinky.internal

import better.files.File
import blinky.v0.{BlinkyConfig, MutantRange}
import metaconfig.Configured
import scalafix.v1._
import zio.json.EncoderOps

import java.util.concurrent.atomic.AtomicInteger
import scala.meta._
import scala.meta.inputs.Input.VirtualFile
import scala.util.Try

class Blinky(config: BlinkyConfig) extends SemanticRule("Blinky") {
  private val mutationId: AtomicInteger = new AtomicInteger(1)
  private val mutantsOutputFileOpt: Option[File] =
    Some(config.mutantsOutputFile).filter(_.nonEmpty).map(File(_))
  mutantsOutputFileOpt.foreach(_.createFileIfNotExists())
  private val specificMutants: Seq[MutantRange] =
    config.specificMutants
  private def nextIndex: Int = mutationId.getAndIncrement()
  private val mutateAll = config.filesToMutate.headOption.exists(_._1 == "all")
  private val fileShouldBeMutated: String => Option[Seq[Range]] =
    if (mutateAll)
      (_: String) => Some(Seq.empty)
    else
      config.filesToMutate
        .map { case (key, rangeSeq) =>
          key -> rangeSeq.map(range => (range.start - 1) to (range.end - 1))
        }
        .toMap
        .get(_: String)

  def this() = this(BlinkyConfig.default)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse(name.value)(BlinkyConfig.default)
      .map(new Blinky(_))

  override def fix(implicit doc: SemanticDocument): Patch = {
    val VirtualFile(fileName, _) = doc.input

    fileShouldBeMutated(fileName) match {
      case None =>
        Patch.empty
      case Some(linesSeq) =>
        val findMutations: FindMutations = new FindMutations(config.activeMutators, doc)

        val shouldMutateF: Tree => Boolean =
          if (mutateAll || linesSeq.isEmpty)
            (_: Tree) => true
          else
            (tree: Tree) => {
              val treeRange = tree.pos.startLine to tree.pos.endLine
              linesSeq.exists { lineRange =>
                !(treeRange.end < lineRange.start || lineRange.end < treeRange.start)
              }
            }

        val (finalPatch, mutantsFound): (Seq[Patch], Seq[Seq[Mutant]]) =
          new findMutations.Finder(shouldMutateF)
            .topTreeMutations(doc.tree)
            .flatMap { case (original, MutatedTerms(mutantsFound, needsParens)) =>
              val mutantsSeq =
                mutantsFound
                  .filterNot(_.structure == original.structure)
                  .flatMap { mutated =>
                    val mutantIndex = nextIndex
                    if (specificMutants.exists(_.contains(mutantIndex)))
                      Some(createMutant(original, mutated, needsParens, fileName, mutantIndex))
                    else
                      None
                  }
              createPatch(mutantsSeq, needsParens = needsParens)
            }
            .unzip

        saveNewMutantsToFile(mutantsFound.flatten)
        finalPatch.asPatch
    }
  }

  private def createPatch(
      mutantSeq: Seq[Mutant],
      needsParens: Boolean
  ): Option[(Patch, Seq[Mutant])] =
    mutantSeq.headOption.map(_.original).map { original =>
      val (_, mutatedStr) =
        mutantSeq.map(mutant => (mutant.id, mutant.mutated)).foldRight((0, original)) {
          case ((id, mutatedTerm), (_, originalTerm)) =>
            val mutantId = Lit.String(s"BLINKY_MUTATION_$id")
            val result =
              q"""if (_root_.scala.sys.env.contains($mutantId)) ($mutatedTerm) else ($originalTerm)"""
            (0, result)
        }

      (Patch.replaceTree(original, syntaxParens(mutatedStr, needsParens)), mutantSeq)
    }

  private def createMutant(
      original: Term,
      mutated: Term,
      needsParens: Boolean,
      fileName: String,
      mutantIndex: Int
  ): Mutant = {
    val pos = original.pos
    val input = pos.input.text

    val mutatedSyntax = syntaxParens(mutated, needsParens)
    val mutatedInput = input.substring(0, pos.start) + mutatedSyntax + input.substring(pos.end)

    val gitDiff: String = calculateGitDiff(original, mutatedInput)
    Mutant(mutantIndex, gitDiff, fileName, original, mutated, needsParens)
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
                os.proc(
                  "git",
                  "diff",
                  "--no-index",
                  originalFile.toString,
                  mutatedFile.toString
                ).call(cwd = os.pwd)
              ).failed.get.toString
                .split("\n")
                .drop(5)
                .mkString("\n")

            gitDiff
          }
        }
    }

  private def saveNewMutantsToFile(mutantsFound: Seq[Mutant]): Unit =
    if (mutantsFound.nonEmpty)
      mutantsOutputFileOpt.foreach { mutantsOutputFile =>
        val jsonMutationReport: Seq[String] =
          mutantsFound.map(mutant => mutant.toMutantFile.toJson)
        mutantsOutputFile.appendLines(jsonMutationReport: _*)
      }

}
