package mutators

import java.io.File

import metaconfig.Configured
import play.api.libs.json.Json
import scalafix.v1._

import scala.meta._
import scala.meta.inputs.Input.VirtualFile

class MutateCode(config: MutateCodeConfig) extends SemanticRule("MutateCode") {

  private var mutationId: Int = 1
  private var allMutationsFound: Seq[Mutation] = Seq.empty

  def nextIndex: Int = {
    val currentId = mutationId
    mutationId += 1
    currentId
  }

  def this() = this(MutateCodeConfig.default)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse("MutateCode")(MutateCodeConfig.default)
      .map(new MutateCode(_))

  override def fix(implicit doc: SemanticDocument): Patch = {

    val fileName =
      doc.input match {
        case VirtualFile(path, _) => path
        case _ => ""
      }

    def replace(original: Term, mutated: Term): Mutation = {
      val mutationIndex = nextIndex
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

      def addLineNumbers(startLine: Int, linesBefore: List[String], linesAfter: List[String]): List[String] = {
        val fileDiffSize = 1 + Math.log10(startLine + Math.max(linesBefore.size, linesAfter.size)).toInt
        linesBefore.zipWithIndex.map { case (line, index) => ("%" + fileDiffSize + "d: %s").format(startLine + index + 1, line) } ++
            linesAfter.zipWithIndex.map { case (line, index) => ("%" + fileDiffSize + "d: %s").format(startLine + index + 1, line) }
      }

      val diffLines: List[String] =
        fileName +:
            addLineNumbers(
              pos.startLine,
              input.substring(startDiffBefore, endDiffBefore).split("\n").toList.map("-" + _),
              mutatedInput.substring(startDiffAfter, endDiffAfter).split("\n").toList.map("+" + _)
            )

      Mutation(mutationIndex, diffLines, original, mutated)
    }

    def createPatch(mutationSeq: Seq[Mutation]): Patch = {
      val original = mutationSeq.head.original
      val (_, mutatedStr) =
        mutationSeq.map(mutation => (mutation.id, mutation.mutated)).foldRight((0, original)) {
          case ((id, mutated), (_, originalTerm)) =>
            val mutationName = Lit.String(s"SCALA_MUTATION_$id")
            val result = q"""if (sys.props.contains($mutationName)) ($mutated) else ($originalTerm)"""
            (0, result)
        }

      Patch.replaceTree(original, mutatedStr.syntax)
    }

    def findAllMutations(term: Term): (Seq[Term], Boolean) = {
      val (mutations, fullReplace) =
        config.activeMutators.map(_.collectMutations(term)).unzip
      (mutations.flatten, fullReplace.exists(identity))
    }

    def topTermMutations(term: Term): Seq[Term] = {
      term match {
        case applyInfix @ Term.ApplyInfix(left, op, targs, rightList) =>
          val (mainMutations, fullReplace) = findAllMutations(applyInfix)
          if (fullReplace)
            mainMutations
          else {
            mainMutations ++
                topTermMutations(left).map(mutated => Term.ApplyInfix(mutated, op, targs, rightList)) ++
                rightList.zipWithIndex.flatMap { case (right, index) => topTermMutations(right).map((_, index)) }
                    .map { case (mutated, index) => Term.ApplyInfix(left, op, targs, rightList.updated(index, mutated)) }
          }
        case applyUnary @ Term.ApplyUnary(op, arg) =>
          val (mainMutations, fullReplace) = findAllMutations(applyUnary)
          if (fullReplace)
            mainMutations
          else {
            mainMutations ++
                topTermMutations(arg).map(mutated => Term.ApplyUnary(op, mutated))
          }
        case apply @ Term.Apply(fun, args) =>
          val (mainMutations, fullReplace) = findAllMutations(apply)
          if (fullReplace)
            mainMutations
          else {
            mainMutations ++
                topTermMutations(fun).map(mutated => Term.Apply(mutated, args)) ++
                args.zipWithIndex.flatMap { case (arg, index) => topTermMutations(arg).map((_, index)) }
                    .map { case (mutated, index) => Term.Apply(fun, args.updated(index, mutated)) }
          }
        case select @ Term.Select(qual, name) =>
          val (mainMutations, fullReplace) = findAllMutations(select)
          if (fullReplace)
            mainMutations
          else {
            mainMutations ++
                topTermMutations(qual).map(mutated => Term.Select(mutated, name))
          }
        case tuple @ Term.Tuple(args) =>
          val (mainMutations, fullReplace) = findAllMutations(tuple)
          if (fullReplace)
            mainMutations
          else {
            mainMutations ++
              args.zipWithIndex.flatMap { case (arg, index) => topTermMutations(arg).map((_, index)) }
                .map { case (mutated, index) => Term.Tuple(args.updated(index, mutated)) }
          }
        case matchTerm @ Term.Match(expr, cases) =>
          val (mainMutations, fullReplace) = findAllMutations(matchTerm)
          if (fullReplace)
            mainMutations
          else {
            mainMutations ++
                topTermMutations(expr).map(mutated => Term.Match(mutated, cases)) ++
                cases.zipWithIndex.flatMap {
                  case (Case(pat, cond, body), index) => topTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
                }.map { case (mutated, index) => Term.Match(expr, cases.updated(index, mutated)) }
          }
        case function @ Term.Function(params, body) =>
          val (mainMutations, fullReplace) = findAllMutations(function)
          if (fullReplace)
            mainMutations
          else {
            mainMutations ++
                topTermMutations(body).map(mutated => Term.Function(params, mutated))
          }
        case other =>
          findAllMutations(other)._1
      }
    }

    def collectPatchesFromTree(tree: Tree): Iterable[(Patch, Seq[Mutation])] = {
      tree match {
        case term: Term =>
          val mutationsFound = topTermMutations(term)

          val mutationSeq =
            mutationsFound.map(mutated => replace(term, mutated))

          if (mutationSeq.nonEmpty) {
            List((createPatch(mutationSeq), mutationSeq))
          } else
            List.empty[(Patch, Seq[Mutation])]
        case _: Type | _: Pat | _: Name.Anonymous | _: Self =>
          List.empty[(Patch, Seq[Mutation])]
        case other =>
          other.children.flatMap(collectPatchesFromTree)
      }
    }

    val patchesAndMutations: Iterable[(Patch, Seq[Mutation])] = collectPatchesFromTree(doc.tree)
    val (finalPatch, mutationsFound) = patchesAndMutations.unzip

    if (config.projectPath.nonEmpty) {
      allMutationsFound = allMutationsFound ++ mutationsFound.flatten
      val jsonMutationReport = allMutationsFound.map(Json.toJson(_)).mkString("[", ",", "]")
      new java.io.PrintWriter(new File(s"${config.projectPath}/mutations.json")) {
        write(jsonMutationReport)
        close()
      }
    }

    finalPatch.asPatch
  }

}
