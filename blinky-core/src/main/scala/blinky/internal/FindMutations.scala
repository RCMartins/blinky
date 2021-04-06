package blinky.internal

import blinky.internal.MutatedTerms.{PlaceholderMutatedTerms, StandardMutatedTerms}
import blinky.v0.Mutator
import scalafix.v1.SemanticDocument

import scala.meta.Term.Placeholder
import scala.meta._

class FindMutations(
    activeMutators: Seq[Mutator],
    placeholders: Placeholders,
    implicit val doc: SemanticDocument
) {

  def topTreeMutations(tree: Tree): Seq[(Term, MutatedTerms)] =
    tree match {
      case term: Term =>
        removeLocation(topTermMutations(term, parensRequired = false))
      case Defn.Val(_, _, _, right) =>
        removeLocation(topTermMutations(right, parensRequired = false))
      case Defn.Var(_, _, _, right) =>
        removeLocation(topTermMutations(right, parensRequired = false))
      case other =>
        other.children.flatMap(topTreeMutations)
    }

  private def addLocation(
      seq: Seq[(Term, MutatedTerms)],
      add: Option[Term] = None
  ): Seq[(Term, Option[Term], MutatedTerms)] =
    seq.map { case (term, mutated) => (term, add, mutated) }

  private def removeLocation(
      seq: Seq[(Term, Option[Term], MutatedTerms)]
  ): Seq[(Term, MutatedTerms)] =
    seq.map { case (term, _, mutated) => (term, mutated) }

  private def findAllMutations(
      term: Term
  ): (Seq[Term], Boolean, Boolean) = {
    val replaces = activeMutators.flatMap(_.getMutator(doc).lift(term))
    (replaces.flatMap(_.terms), replaces.exists(_.fullReplace), replaces.exists(_.needsParens))
  }

  private def topTermMutations(
      term: Option[Term],
      parensRequired: Boolean
  ): Seq[(Term, Option[Term], MutatedTerms)] =
    term.map(topTermMutations(_, parensRequired)).getOrElse(Seq.empty)

  private def topTermMutations(
      term: Term,
      parensRequired: Boolean,
      placeholderLocation: Option[Term] = None,
      overrideOriginal: Option[Term] = None,
      inApplyPart: Boolean = false
  ): Seq[(Term, Option[Term], MutatedTerms)] =
//    println((s"inApplyPart=$inApplyPart", term, s"overrideOriginal=$overrideOriginal"))
    addLocation {
      termMutations(term, placeholderLocation, mainTermsOnly = false, inApplyPart = inApplyPart)
        .flatMap { case (original, placeholderLocation, mutatedTerms) =>
          placeholders
            .replacePlaceholders(original, placeholderLocation, mutatedTerms, inApplyPart)
            .flatMap {
              case (original, mutatedTerms) if original == term && overrideOriginal.nonEmpty =>
                Some((overrideOriginal.get, mutatedTerms))
              case (original, mutatedTerms: StandardMutatedTerms)
                  if parensRequired && original == term =>
                Some((original, mutatedTerms.copy(needsParens = true)))
              case (original, mutatedTerms: PlaceholderMutatedTerms)
                  if parensRequired && original == term =>
                Some((original, mutatedTerms.copy(needsParens = true)))
              case other =>
                Some(other)
            }
        }
    }

  private def topMainTermMutations(
      term: Term,
      placeholderLocation: Option[Term] = None,
      inApplyPart: Boolean = false
  ): Seq[Term] =
    termMutations(
      term,
      placeholderLocation,
      mainTermsOnly = true,
      inApplyPart = inApplyPart
    ).flatMap { case (original, placeholderLocation, mutatedTerms) =>
//      println(("replacePlaceholders", original, placeholderLocation, mutatedTerms, inApplyPart))
      placeholders
        .replacePlaceholders(original, placeholderLocation, mutatedTerms, inApplyPart)
        .map {
          case (_, mutatedTerms: PlaceholderMutatedTerms) =>
            mutatedTerms.mutated.map(_._1)
          case (_, mutatedTerms: StandardMutatedTerms) =>
            mutatedTerms.mutated
        }
    }.flatten

  private def termMutations(
      mainTerm: Term,
      placeholderLocation: Option[Term],
      mainTermsOnly: Boolean,
      inApplyPart: Boolean
  ): Seq[(Term, Option[Term], MutatedTerms)] = {
    def selectSmallerMutation(
        term: Term,
        placeholderLocation: => Option[Term],
        subMutationsWithMain: => Seq[Term],
        subMutationsWithoutMain: => Seq[(Term, Option[Term], MutatedTerms)]
    ): Seq[(Term, Option[Term], MutatedTerms)] = {
      val (mainMutations, fullReplace, needsParens) = findAllMutations(term)
      if (fullReplace) {
        Seq((term, placeholderLocation, mainMutations.toMutated(needsParens = needsParens)))
      } else if (mainMutations.nonEmpty || mainTermsOnly) {
//        println((mainMutations.nonEmpty, mainTermsOnly))
        Seq(
          (
            term,
            placeholderLocation,
            (mainMutations ++ subMutationsWithMain).toMutated(needsParens = needsParens)
          )
        )
      } else {
        subMutationsWithoutMain
      }
    }

    def selectLogic(
        select: Term.Select,
        qual: Term,
        name: Term.Name
    ): Seq[(Term, Option[Term], MutatedTerms)] =
      selectSmallerMutation(
        select,
        placeholderLocation,
        topMainTermMutations(
          qual,
          placeholderLocation = placeholderLocation,
          inApplyPart = inApplyPart
        )
          .map(mutated => Term.Select(mutated, name)),
        topTermMutations(
          qual,
          placeholderLocation = placeholderLocation,
          parensRequired = true,
          inApplyPart = inApplyPart
        )
      )

//    println((mainTermsOnly, mainTerm.structure, mainTerm))
    mainTerm match {
      case applyInfix @ Term.ApplyInfix(left, op, targs, rightList) =>
//        println(("applyInfix", applyInfix, s"inApplyPart=$inApplyPart"))

        val newPlaceholderLocation = Some(applyInfix)
        def applyInfixTopMainTermMutations: Seq[Term.ApplyInfix] =
          topMainTermMutations(left, placeholderLocation = newPlaceholderLocation)
            .map(Term.ApplyInfix(_, op, targs, rightList))

        selectSmallerMutation(
          applyInfix,
          newPlaceholderLocation,
          applyInfixTopMainTermMutations ++ {
            val listOfLists = listTermsMutateMain(rightList)

            val replaceIndex: List[Boolean] =
              rightList.map {
                case Placeholder() => false
                case _             => true
              }

            listOfLists.map { list =>
              Term.ApplyInfix(
                left,
                op,
                targs,
                list
                  .zip(replaceIndex)
                  .map {
                    case (Placeholder(), true) => Term.Name("identity")
                    case (other, _)            => other
                  }
              )
            }
          }, {
            val funcMutations =
              topTermMutations(
                left,
                parensRequired = true,
                placeholderLocation = newPlaceholderLocation
              )

            lazy val applyCountPlaceholders =
              placeholders.countPlaceholders(applyInfix, inApplyPart = inApplyPart)
            val placeholderMismatch =
              funcMutations.map(_._3).exists {
                case StandardMutatedTerms(_, _) =>
                  false
                case PlaceholderMutatedTerms(_, _, mutated, _, _, _) =>
                  mutated
                    .map(term =>
                      placeholders.countFuncPlaceholders(term._1, inApplyPart = inApplyPart)
                    )
                    .exists(_ != applyCountPlaceholders)
              }

            {
              if (placeholderMismatch)
                Seq(
                  (
                    applyInfix,
                    placeholderLocation,
                    applyInfixTopMainTermMutations.toMutated(false)
                  )
                )
              else
                funcMutations
            } ++ {
//              println(rightList)
//              rightList.flatMap(topTermMutations(_, parensRequired = true))
//              ???

              {
                val listOfLists = listTermsMutateMain(rightList)

                val replaceIndex: List[Boolean] =
                  rightList.map {
                    case Placeholder() => false
                    case _             => true
                  }

                val finalMutations =
                  listOfLists
                    .map { list =>
                      Term.ApplyInfix(
                        left,
                        op,
                        targs,
                        list
                          .zip(replaceIndex)
                          .map {
                            case (Placeholder(), true) if inApplyPart => Term.Name("identity")
                            case (other, _)                           => other
                          }
                      )
                    }

//                println("#" * 50)
//                println(
//                  Seq((applyInfix, placeholderLocation, finalMutations.toMutated(false)))
//                )
//                println("#" * 50)

                Seq((applyInfix, placeholderLocation, finalMutations.toMutated(false)))
              }

            }
          }
        )
      case applyUnary @ Term.ApplyUnary(op, arg) =>
        selectSmallerMutation(
          applyUnary,
          None,
          topMainTermMutations(arg).map(mutated => Term.ApplyUnary(op, mutated)),
          topTermMutations(arg, parensRequired = true)
        )
      case apply @ Term.Apply(fun, args) =>
        val newPlaceholderLocation = Some(apply)
        def applyTopMainTermMutations: Seq[Term.Apply] =
          topMainTermMutations(
            fun,
            placeholderLocation = newPlaceholderLocation,
            inApplyPart = true
          )
            .map(mutated => Term.Apply(mutated, args))

        selectSmallerMutation(
          apply,
          newPlaceholderLocation,
          applyTopMainTermMutations ++ {
            val listOfLists = listTermsMutateMain(args)

            val replaceIndex: List[Boolean] =
              args.map {
                case Placeholder() => false
                case _             => true
              }

            listOfLists.map { list =>
              Term.Apply(
                fun,
                list
                  .zip(replaceIndex)
                  .map {
                    case (Placeholder(), true) => Term.Name("identity")
                    case (other, _)            => other
                  }
              )
            }
          }, {
            val funcMutations =
              topTermMutations(
                fun,
                parensRequired = true,
                placeholderLocation = newPlaceholderLocation,
                inApplyPart = true
              )

            lazy val applyCountPlaceholders =
              placeholders.countPlaceholders(apply, inApplyPart = true)
            val placeholderMismatch =
              funcMutations.map(_._3).exists {
                case StandardMutatedTerms(_, _) =>
                  false
                case PlaceholderMutatedTerms(_, _, mutated, _, _, _) =>
                  mutated
                    .map(term => placeholders.countFuncPlaceholders(term._1, inApplyPart = true))
                    .exists(_ != applyCountPlaceholders)
              }

            {
              if (placeholderMismatch)
                Seq(
                  (
                    apply,
                    placeholderLocation,
                    applyTopMainTermMutations.toMutated(false)
                  )
                )
              else
                funcMutations
            } ++
              args.flatMap(topTermMutations(_, parensRequired = false))
          }
        )
      case applyType @ Term.ApplyType(term, targs) =>
        selectSmallerMutation(
          applyType,
          None,
          topMainTermMutations(term).map(mutated => Term.ApplyType(mutated, targs)),
          topTermMutations(term, parensRequired = false, overrideOriginal = Some(applyType))
        )
      case select @ Term.Select(qual, name) =>
        selectLogic(select, qual, name)
      case tuple @ Term.Tuple(args) =>
        selectSmallerMutation(
          tuple,
          None,
          listTermsMutateMain(args).map(Term.Tuple(_)),
          args.flatMap(topTermMutations(_, parensRequired = false))
        )
      case matchTerm @ Term.Match(expr, cases) =>
        selectSmallerMutation(
          matchTerm,
          None,
          topMainTermMutations(expr).map(mutated => Term.Match(mutated, cases)) ++
            cases.zipWithIndex
              .flatMap { case (Case(pat, cond, body), index) =>
                topMainTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
              }
              .map { case (mutated, index) => Term.Match(expr, cases.updated(index, mutated)) },
          cases.flatMap(caseTerm =>
            topTermMutations(caseTerm.cond, parensRequired = true) ++
              topTermMutations(caseTerm.body, parensRequired = false)
          )
        )
      case parFunc @ Term.PartialFunction(cases) =>
        selectSmallerMutation(
          parFunc,
          None,
          cases.zipWithIndex
            .flatMap { case (Case(pat, cond, body), index) =>
              topMainTermMutations(body).map(mutated => (Case(pat, cond, mutated), index))
            }
            .map { case (mutated, index) => Term.PartialFunction(cases.updated(index, mutated)) },
          cases.flatMap(caseTerm =>
            topTermMutations(caseTerm.cond, parensRequired = true) ++
              topTermMutations(caseTerm.body, parensRequired = false)
          )
        )
      case function @ Term.Function(params, body) =>
        selectSmallerMutation(
          function,
          None,
          topMainTermMutations(body).map(mutated => Term.Function(params, mutated)),
          topTermMutations(body, parensRequired = false)
        )
      case assign @ Term.Assign(name, exp) =>
        selectSmallerMutation(
          assign,
          None,
          topMainTermMutations(exp).map(mutated => Term.Assign(name, mutated)),
          topTermMutations(exp, parensRequired = false)
        )
      case block @ Term.Block(stats) =>
        selectSmallerMutation(
          block,
          None,
          Seq.empty, //TODO when the top stats are completely done we should update this
          addLocation(stats.flatMap(topTreeMutations))
        )
      case ifTerm @ Term.If(cond, thenPart, elsePart) =>
        selectSmallerMutation(
          ifTerm,
          None,
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
          None,
          initMutateMain(init).map(Term.New(_)),
          init.argss.flatMap(_.flatMap(topTermMutations(_, parensRequired = false)))
        )
      case newAnonymous @ Term.NewAnonymous(Template(early, inits, self, stats)) =>
        selectSmallerMutation(
          newAnonymous,
          None,
          inits.zipWithIndex
            .flatMap { case (init, index) => initMutateMain(init).map((_, index)) }
            .map { case (mutated, index) =>
              Term.NewAnonymous(Template(early, inits.updated(index, mutated), self, stats))
            } ++
            Seq.empty, //TODO when the top stats are completely done we should update this
          inits
            .flatMap(_.argss.flatMap(_.flatMap(topTermMutations(_, parensRequired = false)))) ++
            addLocation(stats.flatMap(topTreeMutations))
        )
      case repeated @ Term.Repeated(expr) =>
        selectSmallerMutation(
          repeated,
          None,
          topMainTermMutations(expr).map(mutated => Term.Repeated(mutated)),
          topTermMutations(expr, parensRequired = true)
        )
      case ascribe @ Term.Ascribe(expr, tpe) =>
        selectSmallerMutation(
          ascribe,
          None,
          topMainTermMutations(expr).map(mutated => Term.Ascribe(mutated, tpe)),
          topTermMutations(expr, parensRequired = true)
        )
      case other =>
        Seq((mainTerm, None, findAllMutations(other)._1.toMutated(needsParens = false)))
    }
  }

  private def listTermsMutateMain(originalList: List[Term]): List[List[Term]] =
    originalList.zipWithIndex
      .flatMap { case (term, index) => topMainTermMutations(term).map((_, index)) }
      .map { case (mutated, index) => originalList.updated(index, mutated) }

  private def initMutateMain(init: Init): List[Init] =
    init.argss
      .map(_.zipWithIndex)
      .zipWithIndex
      .flatMap { case (args, index) =>
        args.flatMap { case (arg, indexInner) =>
          topMainTermMutations(arg).map((_, (index, indexInner)))
        }
      }
      .map { case (mutated, (index, indexInner)) =>
        val argsUpdated = init.argss(index).updated(indexInner, mutated)
        Init(init.tpe, init.name, init.argss.updated(index, argsUpdated))
      }
}
