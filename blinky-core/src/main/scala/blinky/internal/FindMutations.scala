package blinky.internal

import blinky.v0.Mutator
import scalafix.v1.SemanticDocument

import scala.meta._

class FindMutations(activeMutators: Seq[Mutator], implicit val doc: SemanticDocument) {
  private def findAllMutations(
      term: Term
  ): (Seq[Term], Boolean, Boolean) = {
    val replaces = activeMutators.flatMap(_.getMutator(doc).lift(term))
    (replaces.flatMap(_.terms), replaces.exists(_.fullReplace), replaces.exists(_.needsParens))
  }

  def topTreeMutations(tree: Tree): Seq[(Term, MutatedTerms)] =
    tree match {
      case term: Term =>
        topTermMutations(term)
      case Defn.Val(_, _, _, right) =>
        topTermMutations(right)
      case Defn.Var.After_4_7_2(_, _, _, right) =>
        topTermMutations(right)
      case Defn.Def.After_4_7_3(_, _, paramsClauseList, _, body) =>
        paramsClauseList.flatMap(
          _.paramClauses.flatMap(
            _.flatMap(param => topTermMutations(param.default, parensRequired = false))
          )
        ) ++
          topTermMutations(body)
      case Defn.Object(_, _, template) =>
        template.stats.flatMap(topTreeMutations)
      case Defn.Trait.After_4_6_0(_, _, _, constructor, template) =>
        // TODO paramsClause
        topTreeMutations(constructor) ++
          template.stats.flatMap(topTreeMutations)
      case Defn.Class.After_4_6_0(_, _, _, constructor, template) =>
        // TODO paramsClause
        topTreeMutations(constructor) ++
          template.stats.flatMap(topTreeMutations)
      case _: Defn.Type =>
        Seq.empty
      case Ctor.Primary.After_4_6_0(_, _, paramsClauseList) =>
        paramsClauseList.flatMap(
          _.flatMap(param => topTermMutations(param.default, parensRequired = false))
        )
      case other =>
        other.children.flatMap(topTreeMutations)
    }

  private def topTermMutations(
      termOpt: Option[Term],
      parensRequired: Boolean
  ): Seq[(Term, MutatedTerms)] =
    termOpt.map(topTermMutations(_, parensRequired)).getOrElse(Seq.empty)

  private def topTermMutations(
      term: Term,
      parensRequired: Boolean = false,
      overrideOriginal: Option[Term] = None
  ): Seq[(Term, MutatedTerms)] =
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

  private def topMainTermMutations(term: Term): Seq[Term] =
    termMutations(term, mainTermsOnly = true).flatMap { case (_, mutations) => mutations.mutated }

  private def termMutations(mainTerm: Term, mainTermsOnly: Boolean): Seq[(Term, MutatedTerms)] = {
    def selectSmallerMutation(
        term: Term,
        subMutationsWithMain: => Seq[Term],
        subMutationsWithoutMain: => Seq[(Term, MutatedTerms)]
    ): Seq[(Term, MutatedTerms)] = {
      val (mainMutations, fullReplace, needsParens) = findAllMutations(term)
      if (fullReplace)
        Seq((term, mainMutations.toMutated(needsParens = needsParens)))
      else if (mainMutations.nonEmpty || mainTermsOnly)
        Seq((term, (mainMutations ++ subMutationsWithMain).toMutated(needsParens = needsParens)))
      else
        subMutationsWithoutMain
    }

    mainTerm match {
      case applyInfix @ Term.ApplyInfix.After_4_6_0(left, op, targs, rightList) =>
        selectSmallerMutation(
          applyInfix,
          topMainTermMutations(left)
            .map(mutated => Term.ApplyInfix(mutated, op, targs, rightList)) ++
            listTermsMutateMain(rightList).map(Term.ApplyInfix.After_4_6_0(left, op, targs, _)),
          topTermMutations(left, parensRequired = true) ++
            rightList.flatMap(topTermMutations(_, parensRequired = true))
        )
      case applyUnary @ Term.ApplyUnary(op, arg) =>
        selectSmallerMutation(
          applyUnary,
          topMainTermMutations(arg).map(mutated => Term.ApplyUnary(op, mutated)),
          topTermMutations(arg, parensRequired = true)
        )
      case apply @ Term.Apply.After_4_6_0(fun, args) =>
        selectSmallerMutation(
          apply,
          topMainTermMutations(fun).map(mutated => Term.Apply(mutated, args)) ++
            listTermsMutateMain(args).map(Term.Apply.After_4_6_0(fun, _)),
          topTermMutations(fun, parensRequired = true) ++
            args.flatMap(topTermMutations(_))
        )
      case applyType @ Term.ApplyType.After_4_6_0(term, targs) =>
        selectSmallerMutation(
          applyType,
          topMainTermMutations(term).map(mutated => Term.ApplyType(mutated, targs)),
          topTermMutations(term, overrideOriginal = Some(applyType))
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
          args.flatMap(topTermMutations(_))
        )
      case matchTerm @ Term.Match.After_4_4_5(expr, cases, mods) =>
        selectSmallerMutation(
          matchTerm,
          topMainTermMutations(expr).map(mutated => Term.Match(mutated, cases, mods)) ++
            cases.zipWithIndex
              .flatMap { case (Case(pat, cond, body), index) =>
                topMainTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
              }
              .map { case (mutated, index) =>
                Term.Match(expr, cases.updated(index, mutated), mods)
              },
          cases.flatMap(caseTerm =>
            topTermMutations(caseTerm.cond, parensRequired = true) ++
              topTermMutations(caseTerm.body)
          )
        )
      case parFunc @ Term.PartialFunction(cases) =>
        selectSmallerMutation(
          parFunc,
          cases.zipWithIndex
            .flatMap { case (Case(pat, cond, body), index) =>
              topMainTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
            }
            .map { case (mutated, index) => Term.PartialFunction(cases.updated(index, mutated)) },
          cases.flatMap(caseTerm =>
            topTermMutations(caseTerm.cond, parensRequired = true) ++
              topTermMutations(caseTerm.body)
          )
        )
      case function @ Term.Function.After_4_6_0(params, body) =>
        selectSmallerMutation(
          function,
          topMainTermMutations(body).map(mutated => Term.Function(params, mutated)),
          topTermMutations(body)
        )
      case function @ Term.AnonymousFunction(term) =>
        selectSmallerMutation(
          function,
          topMainTermMutations(term).map(mutated => Term.AnonymousFunction(mutated)),
          topTermMutations(term)
        )
      case assign @ Term.Assign(name, exp) =>
        selectSmallerMutation(
          assign,
          topMainTermMutations(exp).map(mutated => Term.Assign(name, mutated)),
          topTermMutations(exp)
        )
      case block @ Term.Block(stats) =>
        selectSmallerMutation(
          block,
          Seq.empty, // TODO when the top stats are completely done we should update this
          stats.flatMap(topTreeMutations)
        )
      case ifTerm @ Term.If.After_4_4_0(cond, thenPart, elsePart, mods) =>
        selectSmallerMutation(
          ifTerm,
          topMainTermMutations(cond).map(mutated => Term.If(mutated, thenPart, elsePart, mods)) ++
            topMainTermMutations(thenPart).map(mutated => Term.If(cond, mutated, elsePart, mods)) ++
            topMainTermMutations(elsePart).map(mutated => Term.If(cond, thenPart, mutated, mods)),
          topTermMutations(cond) ++
            topTermMutations(thenPart) ++
            topTermMutations(elsePart)
        )
      case forTerm @ Term.For(enumsList, bodyTerm) =>
        selectSmallerMutation(
          forTerm,
          topMainTermMutations(bodyTerm).map(mutated => Term.For(enumsList, mutated)),
          enumsList.flatMap(topTermMutateEnumerator) ++
            topTermMutations(bodyTerm)
        )
      case forYield @ Term.ForYield(enumsList, bodyTerm) =>
        selectSmallerMutation(
          forYield,
          topMainTermMutations(bodyTerm).map(mutated => Term.ForYield(enumsList, mutated)),
          enumsList.flatMap(topTermMutateEnumerator) ++
            topTermMutations(bodyTerm)
        )
      case newTerm @ Term.New(init) =>
        selectSmallerMutation(
          newTerm,
          initMutateMain(init).map(Term.New(_)),
          init.argClauses.flatMap(_.flatMap(topTermMutations(_)))
        )
      case newAnonymous @ Term.NewAnonymous(
            Template.After_4_4_0(early, inits, self, stats, types)
          ) =>
        selectSmallerMutation(
          newAnonymous,
          inits.zipWithIndex
            .flatMap { case (init, index) => initMutateMain(init).map((_, index)) }
            .map { case (mutated, index) =>
              Term.NewAnonymous(Template(early, inits.updated(index, mutated), self, stats, types))
            } ++
            Seq.empty, // TODO when the top stats are completely done we should update this
          inits
            .flatMap(
              _.argClauses.flatMap(_.flatMap(topTermMutations(_)))
            ) ++
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

  private def topTermMutateEnumerator(enumerator: Enumerator): Seq[(Term, MutatedTerms)] =
    enumerator match {
      case Enumerator.CaseGenerator(_, term) => topTermMutations(term)
      case Enumerator.Generator(_, term)     => topTermMutations(term)
      case Enumerator.Guard(term)            => topTermMutations(term, parensRequired = true)
      case Enumerator.Val(_, term)           => topTermMutations(term)
    }

  private def listTermsMutateMain(originalList: List[Term]): List[List[Term]] =
    originalList.zipWithIndex
      .flatMap { case (term, index) => topMainTermMutations(term).map((_, index)) }
      .map { case (mutated, index) => originalList.updated(index, mutated) }

  private def initMutateMain(init: Init): Seq[Init] =
    init.argClauses
      .map(_.zipWithIndex)
      .zipWithIndex
      .flatMap { case (args, index) =>
        args.flatMap { case (arg, indexInner) =>
          topMainTermMutations(arg).map((_, (index, indexInner)))
        }
      }
      .map { case (mutated, (index, indexInner)) =>
        val argsUpdated = init.argClauses(index).updated(indexInner, mutated)
        Init.After_4_6_0(init.tpe, init.name, init.argClauses.updated(index, argsUpdated))
      }
}
