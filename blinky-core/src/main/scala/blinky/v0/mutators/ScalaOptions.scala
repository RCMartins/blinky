package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.{Lit, Term}

object ScalaOptions extends MutatorGroup {
  override val groupName: String = "ScalaOptions"

  override val getSubMutators: List[Mutator] =
    List(
      GetOrElse,
      Exists,
      Forall,
      IsEmpty,
      NonEmpty,
      Fold,
      OrElse,
      OrNull,
      Filter,
      FilterNot,
      Contains
    )

  private object GetOrElse extends SimpleMutator("GetOrElse") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case getOrElse @ Term.Apply.After_4_6_0(
            Term.Select(termName, Term.Name("getOrElse")),
            Term.ArgClause(List(arg), _)
          ) if SymbolMatcher.exact("scala/Option#getOrElse().").matches(getOrElse.symbol) =>
        default(Term.Select(termName, Term.Name("get")), arg)
    }
  }

  private object Exists extends SimpleMutator("Exists") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case exists @ Term.Apply.After_4_6_0(Term.Select(termName, Term.Name("exists")), args)
          if SymbolMatcher.exact("scala/Option#exists().").matches(exists.symbol) =>
        default(Term.Apply(Term.Select(termName, Term.Name("forall")), args))
    }
  }

  private object Forall extends SimpleMutator("Forall") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case forall @ Term.Apply.After_4_6_0(Term.Select(termName, Term.Name("forall")), args)
          if SymbolMatcher.exact("scala/Option#forall().").matches(forall.symbol) =>
        default(Term.Apply(Term.Select(termName, Term.Name("exists")), args))
    }
  }

  private object IsEmpty extends SimpleMutator("IsEmpty") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case isEmpty @ Term.Select(termName, Term.Name("isEmpty"))
          if SymbolMatcher.exact("scala/Option#isEmpty().").matches(isEmpty.symbol) =>
        default(Term.Select(termName, Term.Name("nonEmpty")))
    }
  }

  private object NonEmpty extends SimpleMutator("NonEmpty") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case nonEmpty @ Term.Select(termName, Term.Name("nonEmpty" | "isDefined"))
          if SymbolMatcher
            .exact("scala/Option#nonEmpty().", "scala/Option#isDefined().")
            .matches(nonEmpty.symbol) =>
        default(Term.Select(termName, Term.Name("isEmpty")))
    }
  }

  private object Fold extends SimpleMutator("Fold") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case fold @ Term.Apply.After_4_6_0(
            Term.Apply.After_4_6_0(
              Term.Select(_, Term.Name("fold")),
              Term.ArgClause(List(argDefault), _)
            ),
            _
          ) if SymbolMatcher.exact("scala/Option#fold().").matches(fold.symbol) =>
        default(argDefault)
    }
  }

  private object OrElse extends SimpleMutator("OrElse") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case orElse @ Term.Apply.After_4_6_0(
            Term.Select(termName, Term.Name("orElse")),
            Term.ArgClause(List(arg), _)
          ) if SymbolMatcher.exact("scala/Option#orElse().").matches(orElse.symbol) =>
        default(termName, arg)
    }
  }

  private object OrNull extends SimpleMutator("OrNull") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case orNull @ Term.Select(_, Term.Name("orNull"))
          if SymbolMatcher.exact("scala/Option#orNull().").matches(orNull.symbol) =>
        default(Lit.Null())
    }
  }

  private object Filter extends SimpleMutator("Filter") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case filter @ Term.Apply.After_4_6_0(Term.Select(termName, Term.Name("filter")), args)
          if SymbolMatcher.exact("scala/Option#filter().").matches(filter.symbol) =>
        default(termName, Term.Apply(Term.Select(termName, Term.Name("filterNot")), args))
    }
  }

  private object FilterNot extends SimpleMutator("FilterNot") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case filterNot @ Term.Apply.After_4_6_0(Term.Select(termName, Term.Name("filterNot")), args)
          if SymbolMatcher.exact("scala/Option#filterNot().").matches(filterNot.symbol) =>
        default(termName, Term.Apply(Term.Select(termName, Term.Name("filter")), args))
    }
  }

  private object Contains extends SimpleMutator("Contains") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case contains @ Term.Apply.After_4_6_0(Term.Select(_, Term.Name("contains")), _)
          if SymbolMatcher.exact("scala/Option#contains().").matches(contains.symbol) =>
        default(Lit.Boolean(true), Lit.Boolean(false))
    }
  }

}
