package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.Term

object ScalaTry extends MutatorGroup {
  override val groupName: String = "ScalaTry"

  override val getSubMutators: List[Mutator] =
    List(
      GetOrElse,
      OrElse
    )

  private object GetOrElse extends SimpleMutator("GetOrElse") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case getOrElse @ Term.Apply.After_4_6_0(
            Term.Select(termName, Term.Name("getOrElse")),
            Term.ArgClause(List(arg), _)
          ) if SymbolMatcher.exact("scala/util/Try#getOrElse().").matches(getOrElse.symbol) =>
        default(Term.Select(termName, Term.Name("get")), arg)
    }
  }

  private object OrElse extends SimpleMutator("OrElse") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case orElse @ Term.Apply.After_4_6_0(
            Term.Select(termName, Term.Name("orElse")),
            Term.ArgClause(List(arg), _)
          ) if SymbolMatcher.exact("scala/util/Try#orElse().").matches(orElse.symbol) =>
        default(termName, arg)
    }
  }

}
