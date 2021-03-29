package blinky.internal

import ammonite.ops._
import better.files.File
import blinky.internal.MutatedTerms._
import blinky.v0.{BlinkyConfig, MutantRange}
import metaconfig.Configured
import play.api.libs.json.Json
import scalafix.v1._

import java.util.concurrent.atomic.AtomicInteger
import scala.meta.Term.{Apply, If, Name, Placeholder, Select}
import scala.meta._
import scala.meta.inputs.Input.VirtualFile
import scala.util.Try

class Blinky(config: BlinkyConfig) extends SemanticRule("Blinky") {
  private val mutantId: AtomicInteger = new AtomicInteger(1)
  private val tempVarId: AtomicInteger = new AtomicInteger(1)
  private val mutantsOutputFileOpt: Option[File] =
    Some(config.mutantsOutputFile).filter(_.nonEmpty).map(File(_))
  mutantsOutputFileOpt.foreach(_.createFileIfNotExists())
  private val specificMutants: Seq[MutantRange] =
    config.specificMutants

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
    val VirtualFile(fileName, _) = doc.input

    if (!fileShouldBeMutated(fileName))
      Patch.empty
    else {
      val placeholders: Placeholders =
        new Placeholders(() => Name(s"_BLINKY_TEMP_${nextTempVarIndex}_"))
      val findMutations: FindMutations =
        new FindMutations(config.activeMutators, placeholders, doc)

      def createPatch(
          original: Term,
          mutantSeq: Seq[Mutant],
          placeholderFunction: Term => Term,
          replaceTempVars: Seq[(String, Term)],
          placeholderLocation: Option[Term],
          needsParens: Boolean
      ): Option[(Patch, Seq[Mutant])] =
        mutantSeq.headOption.map(_.original).map { originalReplaced =>
          def replacer(initialTerm: Term): Term = initialTerm
//            replaceTempVars
//              .foldLeft(initialTerm: Tree) { case (updatedTerm, (from, to)) =>
//                updatedTerm.transform { case Term.Name(`from`) => to }
//              }
//              .asInstanceOf[Term]

//          println("#" * 50)
//          println(mutantSeq.mkString("\n"))
//          println("#" * 50)

          val (_, mutatedStrBefore) =
            mutantSeq.map(mutant => (mutant.id, mutant.mutated)).foldRight((0, originalReplaced)) {
              case ((id, mutatedTerm), (_, originalTerm)) =>
                val envContains =
                  Apply(
                    Select(
                      Select(
                        Select(Select(Name("_root_"), Name("scala")), Name("sys")),
                        Name("env")
                      ),
                      Name("contains")
                    ),
                    List(Lit.String(s"BLINKY_MUTATION_$id"))
                  )
                val result =
                  If(envContains, mutatedTerm, originalTerm)
                (0, result)
            }

          placeholderLocation match {
            case None =>
              val mutatedStr = replacer(placeholderFunction(mutatedStrBefore))
              (Patch.replaceTree(original, syntaxParens(mutatedStr, needsParens)), mutantSeq)
            case Some(placeholderLocation) =>
              val mutatedStr = replacer(mutatedStrBefore)
              val placeholderFunctionStr =
                replacer(placeholderFunction(Term.Name("@"))).syntax.takeWhile(_ != '`')
              (
                Patch.replaceTree(original, syntaxParens(mutatedStr, needsParens)) +
                  Patch.addLeft(placeholderLocation, placeholderFunctionStr),
                mutantSeq
              )
          }
        }

      val (finalPatch, mutantsFound): (Seq[Patch], Seq[Seq[Mutant]]) =
        findMutations
          .topTreeMutations(doc.tree)
          .flatMap {
            case (original, StandardMutatedTerms(mutantsFound, needsParens)) =>
              val mutantsSeq =
                mutantsFound
                  .filterNot(_.structure == original.structure)
                  .flatMap { mutated =>
                    val mutantIndex = nextMutantIndex
                    if (specificMutants.exists(_.contains(mutantIndex)))
                      Some(
                        createMutant(
                          original,
                          original,
                          mutated,
                          mutated,
                          needsParens,
                          fileName,
                          mutantIndex
                        )
                      )
                    else
                      None
                  }
              createPatch(
                original,
                mutantsSeq,
                identity,
                Seq.empty,
                None,
                needsParens = needsParens
              )
            case (
                  originalWithP,
                  PlaceholderMutatedTerms(
                    originalWithoutP,
                    placeholderFunction,
                    mutationsFoundNotFiltered,
                    newVars,
                    placeholderLocation,
                    needsParens
                  )
                ) =>
              val mutationsFound = {
                var unique: Set[String] = Set.empty
                mutationsFoundNotFiltered.filter { term =>
                  val syntax = term._2.syntax
                  if (unique(syntax))
                    false
                  else {
                    unique = unique + syntax
                    true
                  }
                }
              }

              val mutantSeq: Seq[Mutant] =
                mutationsFound
                  .filterNot { case (termWithP, _) =>
                    termWithP.structure == originalWithP.structure
                  }
                  .flatMap { case (termWithP, termWithoutP) =>
                    val mutantIndex = nextMutantIndex
                    if (specificMutants.exists(_.contains(mutantIndex)))
                      Some(
                        createMutant(
                          originalWithoutP,
                          originalWithP,
                          termWithoutP,
                          termWithP,
                          needsParens,
                          fileName,
                          mutantIndex
                        )
                      )
                    else
                      None
                  }

              val tempVarsReplaces =
                newVars.map {
                  (_, Term.Name(s"_BLINKY_TEMP_${nextTempVarIndex}_"))
                }

              createPatch(
                originalWithP,
                mutantSeq,
                placeholderFunction,
                tempVarsReplaces,
                placeholderLocation,
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
      mutatedForDiffOriginal: Term,
      needsParens: Boolean,
      fileName: String,
      mutantIndex: Int
  ): Mutant = {
    val pos = originalForDiff.pos
    val input = pos.input.text

    val mutatedForDiff =
      mutatedForDiffOriginal match {
        case Placeholder() => Term.Name("identity")
        case other         => other
      }

    val mutatedSyntax = syntaxParens(mutatedForDiff, needsParens)
    val mutatedStr = input.substring(0, pos.start) + mutatedSyntax + input.substring(pos.end)

    val gitDiff: String = calculateGitDiff(originalForDiff, mutatedStr)

    Mutant(
      mutantIndex,
      gitDiff,
      fileName,
      original,
      originalForDiff,
      mutated,
      mutatedForDiff,
      needsParens
    )
  }

  def calculateGitDiff(original: Term, mutatedStr: String): String =
    mutantsOutputFileOpt match {
      case None =>
        ""
      case Some(_) =>
        File.temporaryFile() { originalFile =>
          originalFile.writeText(original.pos.input.text)

          File.temporaryFile() { mutatedFile =>
            mutatedFile.writeText(mutatedStr)

            val gitDiff =
              Try(
                %%(
                  "git",
                  "diff",
                  "--no-index",
                  originalFile.toString,
                  mutatedFile.toString
                )(pwd)
              ).failed.get.toString
                .split("\n")
                .drop(5)
                .mkString("\n")

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
