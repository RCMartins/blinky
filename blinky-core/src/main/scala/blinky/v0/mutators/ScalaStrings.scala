package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default, fullReplace}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.{Lit, Term}

object ScalaStrings extends MutatorGroup {
  override val groupName: String = "ScalaStrings"

  override val getSubMutators: List[Mutator] =
    List(
      Mutators.Concat,
      Mutators.ToUpperCase,
      Mutators.ToLowerCase,
      Mutators.Trim,
      Mutators.Capitalize,
      Mutators.StripPrefix,
      Mutators.StripSuffix,
      Mutators.Map,
      Mutators.FlatMap,
      Mutators.DropWhile,
      Mutators.TakeWhile
    )

  object Mutators {

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

    val Trim: SimpleMutator =
      StringMutatorSelect("Trim", "trim", "java/lang/String#trim().")

    val Capitalize: SimpleMutator =
      StringMutatorSelect("Capitalize", "capitalize", "scala/collection/StringOps#capitalize().")

    val StripPrefix: SimpleMutator =
      StringMutatorApply("StripPrefix", "stripPrefix", "scala/collection/StringOps#stripPrefix().")

    val StripSuffix: SimpleMutator =
      StringMutatorApply("StripSuffix", "stripSuffix", "scala/collection/StringOps#stripSuffix().")

    val Map: SimpleMutator =
      StringMutatorApply("Map", "map", "scala/collection/StringOps#map(+1).")

    val FlatMap: SimpleMutator =
      StringMutatorApply("FlatMap", "flatMap", "scala/collection/StringOps#flatMap(+1).")

    val DropWhile: SimpleMutator =
      StringMutatorApply("DropWhile", "dropWhile", "scala/collection/StringOps#dropWhile().")

    val TakeWhile: SimpleMutator =
      StringMutatorApply("TakeWhile", "takeWhile", "scala/collection/StringOps#takeWhile().")

  }

  private case class StringMutatorSelect(
      mutatorName: String,
      opName: String,
      symbolMatch: String
  ) extends SimpleMutator(mutatorName) {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case op @ Term.Select(term @ _, Term.Name(`opName`))
          if SymbolMatcher.exact(symbolMatch).matches(op.symbol) =>
        default(term)
    }
  }

  private case class StringMutatorApply(
      mutatorName: String,
      opName: String,
      symbolMatch: String
  ) extends SimpleMutator(mutatorName) {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case op @ Term.Apply(Term.Select(term @ _, Term.Name(`opName`)), _)
          if SymbolMatcher.exact(symbolMatch).matches(op.symbol) =>
        default(term)
    }
  }

}
