package mutators

import java.io.{File, FileWriter}
import java.util.concurrent.atomic.AtomicInteger

import metaconfig.Configured
import play.api.libs.json.Json
import scalafix.v1._

import scala.meta._
import scala.meta.inputs.Input.VirtualFile

class MutateCode(config: MutateCodeConfig) extends SemanticRule("MutateCode") {

  private val mutationId: AtomicInteger = new AtomicInteger(1)
  private val mutatorsPathOption: Option[File] =
    if (config.mutatorsPath.nonEmpty) Some(new File(config.mutatorsPath))
    else if (config.projectPath.nonEmpty) Some(new File(config.projectPath))
    else None

  private def nextIndex: Int = mutationId.getAndIncrement()

  def this() = this(MutateCodeConfig.default)

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse("MutateCode")(MutateCodeConfig.default)
      .map(new MutateCode(_))

  override def fix(implicit doc: SemanticDocument): Patch = {

    val fileName =
      doc.input match {
        case VirtualFile(path, _) => path
        case _                    => ""
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

      Mutation(mutationIndex, diffLines, original, mutated)
    }

    def createPatch(
        mutationSeq: Seq[Mutation],
        needsParens: Boolean
    ): Option[(Patch, Seq[Mutation])] = {
      mutationSeq match {
        case Mutation(_, _, original, _, _) +: _ =>
          val (_, mutatedStr) =
            mutationSeq.map(mutation => (mutation.id, mutation.mutated)).foldRight((0, original)) {
              case ((id, mutated), (_, originalTerm)) =>
                val mutationName = Lit.String(s"SCALA_MUTATION_$id")
                val result =
                  q"""if (sys.props.contains($mutationName)) ($mutated) else ($originalTerm)"""
                (0, result)
            }

          val finalSyntax = if (needsParens) "(" + mutatedStr.syntax + ")" else mutatedStr.syntax
          Some(Patch.replaceTree(original, finalSyntax), mutationSeq)
        case _ =>
          None
      }
    }

    def findAllMutations(term: Term): (Seq[Term], Boolean) = {
      val (mutations, fullReplace) =
        config.activeMutators.map(_.collectMutations(term)).unzip
      (mutations.flatten, fullReplace.exists(identity))
    }

    def collectPatchesFromTree(tree: Tree): Iterable[(Patch, Seq[Mutation])] = {
      topTreeMutations(tree).flatMap {
        case (term, MutatedTerms(mutationsFound, needsParens)) =>
          val mutationSeq = mutationsFound.map(mutated => replace(term, mutated))
          createPatch(mutationSeq, needsParens = needsParens)
      }
    }

    def topTreeMutations(tree: Tree): Seq[(Term, MutatedTerms)] = {
      tree match {
        case term: Term =>
          topTermMutations(term, parensRequired = false)
        case other =>
          other.children.flatMap(topTreeMutations)
      }
    }

    def topTermMutations(
        term: Term,
        parensRequired: Boolean,
        overrideOriginal: Option[Term] = None
    ): Seq[(Term, MutatedTerms)] = {
      termMutations(term, mainTermsOnly = false).collect {
        // Disable rules on Apply Term.Placeholder until we can handle this case properly
        case (original, _)
            if original.collect { case Term.Apply(_, List(Term.Placeholder())) => }.nonEmpty =>
          None
        case (original, mutatedTerms) if parensRequired && original == term =>
          Some((original, mutatedTerms.copy(needsParens = true)))
        case (original, mutatedTerms) if original == term && overrideOriginal.nonEmpty =>
          Some((overrideOriginal.get, mutatedTerms))
        case other =>
          Some(other)
      }.flatten
    }

    def topMainTermMutations(term: Term): Seq[Term] = {
      // Disable rules on Apply Term.Placeholder until we can handle this case properly
      if (term.collect { case Term.Apply(_, list) if list.contains(Term.Placeholder()) => () }.nonEmpty)
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
          Seq((term, mainMutations.toMutated(needsParens = false)))
        else if (mainMutations.nonEmpty || mainTermsOnly) {
          Seq((term, (mainMutations ++ subMutationsWithMain).toMutated(needsParens = false)))
        } else {
          subMutationsWithoutMain
        }
      }

      def listTermsMutateMain(originalList: List[Term]): List[List[Term]] = {
        originalList.zipWithIndex
          .flatMap { case (term, index) => topMainTermMutations(term).map((_, index)) }
          .map { case (mutated, index) => originalList.updated(index, mutated) }
      }

      def initMutateMain(init: Init): List[Init] = {
        init.argss
          .map(_.zipWithIndex)
          .zipWithIndex
          .flatMap {
            case (args, index) =>
              args.flatMap {
                case (arg, indexInner) => topMainTermMutations(arg).map((_, (index, indexInner)))
              }
          }
          .map {
            case (mutated, (index, indexInner)) =>
              val argsUpdated = init.argss(index).updated(indexInner, mutated)
              Init(init.tpe, init.name, init.argss.updated(index, argsUpdated))
          }
      }

      mainTerm match {
        case applyInfix @ Term.ApplyInfix(left, op, targs, rightList) =>
          selectSmallerMutation(
            applyInfix,
            topMainTermMutations(left)
              .map(mutated => Term.ApplyInfix(mutated, op, targs, rightList)) ++
              listTermsMutateMain(rightList).map(Term.ApplyInfix(left, op, targs, _)),
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
              listTermsMutateMain(args).map(Term.Apply(fun, _)),
            topTermMutations(fun, parensRequired = false) ++
              args.flatMap(topTermMutations(_, parensRequired = false))
          )
        case applyType @ Term.ApplyType(term, targs) =>
          selectSmallerMutation(
            applyType,
            topMainTermMutations(term).map(mutated => Term.ApplyType(mutated, targs)),
            topTermMutations(term, parensRequired = false, overrideOriginal = Some(applyType))
          )
        case select @ Term.Select(qual, name) =>
          selectSmallerMutation(
            select,
            topMainTermMutations(qual).map(mutated => Term.Select(mutated, name)),
            topTermMutations(qual, parensRequired = true)
          )
        case tuple @ Term.Tuple(args) =>
          selectSmallerMutation(
            tuple,
            listTermsMutateMain(args).map(Term.Tuple(_)),
            args.flatMap(topTermMutations(_, parensRequired = false))
          )
        case matchTerm @ Term.Match(expr, cases) =>
          selectSmallerMutation(
            matchTerm,
            topMainTermMutations(expr).map(mutated => Term.Match(mutated, cases)) ++
              cases.zipWithIndex
                .flatMap {
                  case (Case(pat, cond, body), index) =>
                    topMainTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
                }
                .map { case (mutated, index) => Term.Match(expr, cases.updated(index, mutated)) },
            cases.flatMap(caseTerm => topTermMutations(caseTerm.body, parensRequired = false))
          )
        case parFunc @ Term.PartialFunction(cases) =>
          selectSmallerMutation(
            parFunc,
            cases.zipWithIndex
              .flatMap {
                case (Case(pat, cond, body), index) =>
                  topMainTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
              }
              .map { case (mutated, index) => Term.PartialFunction(cases.updated(index, mutated)) },
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
            Seq.empty, //TODO when the top stats are completely done we should update this
            stats.flatMap(topTreeMutations)
          )
        case ifTerm @ Term.If(cond, thenPart, elsePart) =>
          selectSmallerMutation(
            ifTerm,
            topMainTermMutations(cond).map(mutated => Term.If(mutated, thenPart, elsePart)) ++
              topMainTermMutations(thenPart).map(mutated => Term.If(cond, mutated, elsePart)) ++
              topMainTermMutations(elsePart).map(mutated => Term.If(cond, thenPart, mutated)),
            topTermMutations(cond, parensRequired = false) ++
              topTermMutations(thenPart, parensRequired = false) ++
              topTermMutations(elsePart, parensRequired = false)
          )
        case newTerm @ Term.New(init) =>
          selectSmallerMutation(
            newTerm,
            initMutateMain(init).map(Term.New(_)),
            init.argss.flatMap(_.flatMap(topTermMutations(_, parensRequired = false)))
          )
        case newAnonymous @ Term.NewAnonymous(Template(early, inits, self, stats)) =>
          selectSmallerMutation(
            newAnonymous,
            inits.zipWithIndex
              .flatMap { case (init, index) => initMutateMain(init).map((_, index)) }
              .map {
                case (mutated, index) =>
                  Term.NewAnonymous(Template(early, inits.updated(index, mutated), self, stats))
              } ++
              Seq.empty, //TODO when the top stats are completely done we should update this
            inits
              .flatMap(_.argss.flatMap(_.flatMap(topTermMutations(_, parensRequired = false)))) ++
              stats.flatMap(topTreeMutations)
          )
        case repeated @ Term.Repeated(expr) =>
          selectSmallerMutation(
            repeated,
            topMainTermMutations(expr).map(mutated => Term.Repeated(mutated)),
            topTermMutations(expr, parensRequired = true)
          )
        case ascribe @ Term.Ascribe(expr, tpe) =>
          selectSmallerMutation(
            ascribe,
            topMainTermMutations(expr).map(mutated => Term.Ascribe(mutated, tpe)),
            topTermMutations(expr, parensRequired = true)
          )
        case other =>
          Seq((mainTerm, findAllMutations(other)._1.toMutated(needsParens = false)))
      }
    }

    {
      val patchesAndMutations: Iterable[(Patch, Seq[Mutation])] = collectPatchesFromTree(doc.tree)
      val (finalPatch, mutationsFoundIterable) = patchesAndMutations.unzip

      mutatorsPathOption.foreach { mutatorsPath =>
        val mutationsFound = mutationsFoundIterable.flatten
        if (mutationsFound.nonEmpty) {
          val jsonMutationReport = mutationsFound.map(Json.toJson(_)).mkString("", "\n", "\n")
          new FileWriter(new File(mutatorsPath, "mutations.json"), true) {
            write(jsonMutationReport)
            close()
          }
        }
      }
      finalPatch.asPatch
    }
  }

}
