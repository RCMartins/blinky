package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.Term

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

  override val getSubMutators: List[Mutator] =
    List(
      When,
      Unless,
    )

}
