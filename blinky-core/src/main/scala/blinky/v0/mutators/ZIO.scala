package blinky.v0.mutators

import blinky.v0.Mutator._
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.Pat.Wildcard
import scala.meta.{Enumerator, Lit, Term}

object ZIO extends MutatorGroup {
  override val groupName: String = "ZIO"

  private val When: SimpleMutator =
    new SimpleMutator("When") {
      override def getMutator(implicit doc: SemanticDocument): MutationResult = {
        case when @ Term.Apply.After_4_6_0(Term.Select(zio, Term.Name("when")), paramList)
            if SymbolMatcher
              .exact("zio/ZIO#when().")
              .matches(when.symbol) =>
          default(
            Term.Apply(Term.Select(zio, Term.Name("unless")), paramList),
            Term.Select(zio, Term.Name("asSome")),
            Term.Apply.After_4_6_0(Term.Select(zio, Term.Name("as")), List(Term.Name("None"))),
          )
      }
    }

  private val Unless: SimpleMutator =
    new SimpleMutator("Unless") {
      override def getMutator(implicit doc: SemanticDocument): MutationResult = {
        case when @ Term.Apply.After_4_6_0(Term.Select(zio, Term.Name("unless")), paramList)
            if SymbolMatcher
              .exact("zio/ZIO#unless().")
              .matches(when.symbol) =>
          default(
            Term.Apply(Term.Select(zio, Term.Name("when")), paramList),
            Term.Select(zio, Term.Name("asSome")),
            Term.Apply.After_4_6_0(Term.Select(zio, Term.Name("as")), List(Term.Name("None"))),
          )
      }
    }

  private val ForYield: SimpleMutator =
    new SimpleMutator("ForYield") {
      private def forYieldRemoveAll(implicit doc: SemanticDocument): MutationSimpleResult = {
        case Term.ForYield(enums, finalTerm: Lit)
            if enums.sizeIs >= 2 &&
              enums.exists {
                case Enumerator.Generator(_, term)
                    if getSymbolType(term).exists(SymbolMatcher.exact("zio/ZIO#").matches) =>
                  true
                case _ =>
                  false
              } =>
          finalTerm match {
            case Lit.Unit() =>
              List(Term.Select(Term.Select(Term.Name("zio"), Term.Name("ZIO")), Term.Name("unit")))
            case _ =>
              List(
                Term.Apply.After_4_6_0(
                  Term
                    .Select(Term.Select(Term.Name("zio"), Term.Name("ZIO")), Term.Name("succeed")),
                  Term.ArgClause(List(finalTerm))
                )
              )
          }
      }

      private def forYieldRemoveOne(implicit doc: SemanticDocument): MutationSimpleResult = {
        case forYield @ Term.ForYield(enums, _) if enums.sizeIs >= 2 =>
          val isSecondEnumAGenerator: Boolean = enums(1) match {
            case Enumerator.Generator(_, _) => true
            case _                          => false
          }

          enums.zipWithIndex
            .foldLeft((List.empty[Term], isSecondEnumAGenerator)) {
              case ((acc, safeRemove), (Enumerator.Generator(_: Wildcard, term), index))
                  if safeRemove &&
                    getSymbolType(term).exists(SymbolMatcher.exact("zio/ZIO#").matches) =>
                (forYield.copy(enums = enums.patch(index, Nil, 1)) :: acc, true)
              case ((acc, _), _) =>
                (acc, true)
            }
            ._1
            .reverse
      }

      override def getMutator(implicit doc: SemanticDocument): MutationResult = { term: Term =>
        default(
          forYieldRemoveAll.applyOrElse(term, (_: Term) => Nil) ++
            forYieldRemoveOne.applyOrElse(term, (_: Term) => Nil)
        )
      }

    }

  override val getSubMutators: List[Mutator] =
    List(
      When,
      Unless,
      ForYield
    )
}
