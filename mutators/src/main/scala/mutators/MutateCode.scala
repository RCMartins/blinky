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

    def createPatch(mutationSeq: Seq[Mutation], needsParens: Boolean): Patch = {
      val original = mutationSeq.head.original
      val (_, mutatedStr) =
        mutationSeq.map(mutation => (mutation.id, mutation.mutated)).foldRight((0, original)) {
          case ((id, mutated), (_, originalTerm)) =>
            val mutationName = Lit.String(s"SCALA_MUTATION_$id")
            val result = q"""if (sys.props.contains($mutationName)) ($mutated) else ($originalTerm)"""
            (0, result)
        }

      if (needsParens)
        Patch.replaceTree(original, "(" + mutatedStr.syntax + ")")
      else
        Patch.replaceTree(original, mutatedStr.syntax)
    }

    def findAllMutations(term: Term): (Seq[Term], Boolean) = {
      val (mutations, fullReplace) =
        config.activeMutators.map(_.collectMutations(term)).unzip
      (mutations.flatten, fullReplace.exists(identity))
    }

    def topStatMutations(stat: Stat): Seq[(Term, MutatedTerms)] = {
      stat match {
        case Defn.Val(_, _, _, term) =>
          topTermMutations(term, parensRequired = false)
        case term: Term =>
          topTermMutations(term, parensRequired = false)
        case other =>
          Seq.empty
      }
    }

    def topTermMutations(term: Term, parensRequired: Boolean): Seq[(Term, MutatedTerms)] = {
      // Disable rules on Apply Term.Placeholder until we can handle this case properly
      if (term.collect { case Term.Apply(_, List(Term.Placeholder())) => }.nonEmpty)
        Seq.empty
      else
        termMutations(term, mainTermsOnly = false).collect {
          case (original, mutatedTerms) if parensRequired && original == term => (original, mutatedTerms.copy(needsParens = true))
          case other => other
        }
    }

    def topMainTermMutations(term: Term): Seq[Term] = {
      // Disable rules on Apply Term.Placeholder until we can handle this case properly
      if (term.collect { case Term.Apply(_, List(Term.Placeholder())) => }.nonEmpty)
        Seq.empty
      else
        termMutations(term, mainTermsOnly = true).flatMap(_._2.mutated)
    }

    def termMutations(mainTerm: Term, mainTermsOnly: Boolean): Seq[(Term, MutatedTerms)] = {
      def selectSmallerMutation(
          term: Term,
          subMutationsWithMain: => Seq[Term],
          subMutationsWithoutMain: => Seq[(Term, MutatedTerms)]
      ): Seq[(Term, MutatedTerms)] = {
        val (mainMutations, fullReplace) = findAllMutations(term)
        if (fullReplace)
          Seq((term, mainMutations.toMutation(false)))
        else if (mainMutations.nonEmpty || mainTermsOnly) {
          Seq((term, (mainMutations ++ subMutationsWithMain).toMutation(false)))
        } else {
          subMutationsWithoutMain
        }
      }

      mainTerm match {
        case applyInfix @ Term.ApplyInfix(left, op, targs, rightList) =>
          selectSmallerMutation(
            applyInfix,
            topMainTermMutations(left).map(mutated => Term.ApplyInfix(mutated, op, targs, rightList)) ++
              rightList.zipWithIndex.flatMap { case (right, index) => topMainTermMutations(right).map((_, index)) }
                .map { case (mutated, index) => Term.ApplyInfix(left, op, targs, rightList.updated(index, mutated)) },
            topTermMutations(left, parensRequired = true) ++
              rightList.flatMap(topTermMutations(_, parensRequired = true))
          )
        case applyUnary @ Term.ApplyUnary(op, arg) =>
          selectSmallerMutation(
            applyUnary,
            topMainTermMutations(arg).map(mutated => Term.ApplyUnary(op, mutated)),
            topTermMutations(arg, parensRequired = true)
          )
        case apply @ Term.Apply(fun, args) =>
          selectSmallerMutation(
            apply,
            topMainTermMutations(fun).map(mutated => Term.Apply(mutated, args)) ++
              args.zipWithIndex.flatMap { case (arg, index) => topMainTermMutations(arg).map((_, index)) }
                .map { case (mutated, index) => Term.Apply(fun, args.updated(index, mutated)) },
            topTermMutations(fun, parensRequired = false) ++
              args.flatMap(topTermMutations(_, parensRequired = false))
          )
        case select @ Term.Select(qual, name) =>
          selectSmallerMutation(
            select,
            topMainTermMutations(qual).map(mutated => Term.Select(mutated, name)),
            topTermMutations(qual, parensRequired = false)
          )
        case tuple @ Term.Tuple(args) =>
          selectSmallerMutation(
            tuple,
            args.zipWithIndex.flatMap { case (arg, index) => topMainTermMutations(arg).map((_, index)) }
              .map { case (mutated, index) => Term.Tuple(args.updated(index, mutated)) },
            args.flatMap(topTermMutations(_, parensRequired = false))
          )
        case matchTerm @ Term.Match(expr, cases) =>
          selectSmallerMutation(
            matchTerm,
            topMainTermMutations(expr).map(mutated => Term.Match(mutated, cases)) ++
              cases.zipWithIndex.flatMap {
                case (Case(pat, cond, body), index) => topMainTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
              }.map { case (mutated, index) => Term.Match(expr, cases.updated(index, mutated)) },
            cases.flatMap(caseTerm => topTermMutations(caseTerm.body, parensRequired = false))
          )
        case function @ Term.Function(params, body) =>
          selectSmallerMutation(
            function,
            topMainTermMutations(body).map(mutated => Term.Function(params, mutated)),
            topTermMutations(body, parensRequired = false)
          )
        case assign @ Term.Assign(name, exp) =>
          selectSmallerMutation(
            assign,
            topMainTermMutations(exp).map(mutated => Term.Assign(name, mutated)),
            topTermMutations(exp, parensRequired = false)
          )
        case block @ Term.Block(stats) =>
          selectSmallerMutation(
            block,
            Seq.empty,
            stats.flatMap(topStatMutations)
          )
        case other =>
          Seq((mainTerm, findAllMutations(other)._1.toMutation(false)))
      }
    }

    def collectPatchesFromTree(tree: Tree): Iterable[(Patch, Seq[Mutation])] = {
      tree match {
        case mainTerm: Term =>
          topTermMutations(mainTerm, parensRequired = false).flatMap {
            case (term, MutatedTerms(mutationsFound, needsParens)) =>
              val mutationSeq =
                mutationsFound.map(mutated => replace(term, mutated))

              if (mutationSeq.nonEmpty) {
                Some((createPatch(mutationSeq, needsParens = needsParens), mutationSeq))
              } else {
                None
              }
          }
        case _: Type | _: Pat | _: Name.Anonymous | _: Self =>
          List.empty[(Patch, Seq[Mutation])]
        case other =>
          other.children.flatMap(collectPatchesFromTree)
      }
    }

    {
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

}
