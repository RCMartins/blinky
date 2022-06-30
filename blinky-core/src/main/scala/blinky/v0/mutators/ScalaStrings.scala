package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default, fullReplace}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.{Lit, Term}

object ScalaStrings extends MutatorGroup {
  override val groupName: String = "ScalaStrings"

  override val getSubMutators: List[Mutator] =
    List(
      Concat,
      Trim,
      ToUpperCase,
      ToLowerCase
    )

  object Concat extends SimpleMutator("Concat") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case concat @ Term.ApplyInfix(_, Term.Name("+"), _, _)
          if SymbolMatcher.exact("java/lang/String#`+`().").matches(concat.symbol) =>
        fullReplace(Lit.String("mutated!"), Lit.String(""))
      case concat @ Term.Apply(Term.Select(_, Term.Name("concat")), _)
          if SymbolMatcher.exact("java/lang/String#concat().").matches(concat.symbol) =>
        fullReplace(Lit.String("mutated!"), Lit.String(""))
    }
  }

  object Trim extends SimpleMutator("Trim") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case trim @ Term.Select(term @ _, Term.Name("trim"))
          if SymbolMatcher.exact("java/lang/String#trim().").matches(trim.symbol) =>
        default(term)
    }
  }

  object ToUpperCase extends SimpleMutator("ToUpperCase") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case toUpperCase @ Term.Select(term @ _, Term.Name("toUpperCase"))
          if SymbolMatcher
            .exact("java/lang/String#toUpperCase(+1).")
            .matches(toUpperCase.symbol) =>
        default(term)
      case toUpperCase @ Term.Apply(Term.Select(term @ _, Term.Name("toUpperCase")), _)
          if SymbolMatcher
            .exact("java/lang/String#toUpperCase().")
            .matches(toUpperCase.symbol) =>
        default(term)
    }
  }

  object ToLowerCase extends SimpleMutator("ToLowerCase") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case toLowerCase @ Term.Select(term @ _, Term.Name("toLowerCase"))
          if SymbolMatcher
            .exact("java/lang/String#toLowerCase(+1).")
            .matches(toLowerCase.symbol) =>
        default(term)
      case toLowerCase @ Term.Apply(Term.Select(term @ _, Term.Name("toLowerCase")), _)
          if SymbolMatcher
            .exact("java/lang/String#toLowerCase().")
            .matches(toLowerCase.symbol) =>
        default(term)
    }
  }

}
