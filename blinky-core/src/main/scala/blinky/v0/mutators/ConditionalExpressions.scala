package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.Term

object ConditionalExpressions extends MutatorGroup {
  override val groupName: String = "ConditionalExpressions"

  override val getSubMutators: List[Mutator] =
    List(AndToOr, OrToAnd, RemoveUnaryNot)

  private object AndToOr extends SimpleMutator("AndToOr") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case and @ Term.ApplyInfix.After_4_6_0(left, Term.Name("&&"), targs, right)
          if SymbolMatcher.exact("scala/Boolean#`&&`().").matches(and.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("||"), targs, right))
    }
  }

  private object OrToAnd extends SimpleMutator("OrToAnd") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case or @ Term.ApplyInfix.After_4_6_0(left, Term.Name("||"), targs, right)
          if SymbolMatcher.exact("scala/Boolean#`||`().").matches(or.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("&&"), targs, right))
    }
  }

  private object RemoveUnaryNot extends SimpleMutator("RemoveUnaryNot") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case boolNeg @ Term.ApplyUnary(Term.Name("!"), arg)
          if SymbolMatcher.exact("scala/Boolean#`unary_!`().").matches(boolNeg.symbol) =>
        default(arg)
    }
  }

}
