package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.SemanticDocument

import scala.meta.{Lit, Term}

object LiteralStrings extends MutatorGroup {
  override val groupName: String = "LiteralStrings"

  override val getSubMutators: List[Mutator] =
    List(
      EmptyToMutated,
      EmptyInterToMutated,
      NonEmptyToMutated,
      NonEmptyInterToMutated
    )

  object EmptyToMutated extends SimpleMutator("EmptyToMutated") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Lit.String(value) if value.isEmpty =>
        default(Lit.String("mutated!"))
    }
  }

  object EmptyInterToMutated extends SimpleMutator("EmptyInterToMutated") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Term.Interpolate(Term.Name("s" | "f" | "raw"), List(Lit.String("")), List()) =>
        default(Lit.String("mutated!"))
    }
  }

  object NonEmptyToMutated extends SimpleMutator("NonEmptyToMutated") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Lit.String(value) if value.nonEmpty =>
        default(Lit.String(""), Lit.String("mutated!"))
    }
  }

  object NonEmptyInterToMutated extends SimpleMutator("NonEmptyInterToMutated") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Term.Interpolate(Term.Name("s" | "f" | "raw"), lits, names)
          if names.nonEmpty || lits.exists { case Lit.String(str) =>
            str.nonEmpty
          } =>
        default(Lit.String(""), Lit.String("mutated!"))
    }
  }

}
