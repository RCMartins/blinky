package mutators

import scalafix.v1.SemanticDocument

import scala.meta._

class FindMutations(activeMutators: Seq[MutationType], implicit val doc: SemanticDocument) {

  def findAllMutations(
      term: Term
  ): (Seq[Term], Boolean) = {
    val (mutations, fullReplace) =
      activeMutators.map(_.collectMutations(term)).unzip
    (mutations.flatten, fullReplace.exists(identity))
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
      // Disable rules on Term.Placeholder until we can handle this case properly
      case (original, _) if original.collect { case Term.Placeholder() => }.nonEmpty =>
        None
      case (original, mutatedTerms) if parensRequired && original == term =>
        Some((original, mutatedTerms.copy(needsParens = true)))
      case (original, mutatedTerms) if original == term && overrideOriginal.nonEmpty =>
        Some((overrideOriginal.get, mutatedTerms))
      case other =>
        Some(other)
    }.flatten
  }

  def topMainTermMutations(term: Term): Seq[Term] =
    termMutations(term, mainTermsOnly = true).flatMap(_._2.mutated)

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
}
