package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.annotation.tailrec
import scala.meta.Term

object Collections extends MutatorGroup {
  override val groupName: String = "Collections"

  private val MaxSize = 25

  @tailrec
  private def removeOneArg(
      before: List[Term],
      terms: List[Term],
      result: List[List[Term]]
  ): List[List[Term]] =
    terms match {
      case Nil =>
        result
      case term :: others =>
        removeOneArg(before :+ term, others, before ++ others :: result)
    }

  private class RemoveApplyArgMutator(
      mutatorName: String,
      collectionName: String,
      val symbolsToMatch: Seq[String],
      minimum: Int
  ) extends SimpleMutator(mutatorName) {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case collection @ Term.Apply(
            select @ (Term.Name(`collectionName`) | Term.Select(_, Term.Name(`collectionName`))),
            args
          )
          if args.lengthCompare(minimum) >= 0 && args.lengthCompare(MaxSize) <= 0 &&
            SymbolMatcher.exact(symbolsToMatch: _*).matches(collection.symbol) =>
        default(removeOneArg(Nil, args, Nil).reverse.map(Term.Apply(select, _)): _*)
    }
  }

  private class RemoveOpMutator(
      mutatorName: String,
      opName: String,
      val symbolsToMatch: Seq[String]
  ) extends SimpleMutator(mutatorName) {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case collection @ Term.Apply(Term.Select(term @ _, Term.Name(`opName`)), _)
          if SymbolMatcher.exact(symbolsToMatch: _*).matches(collection.symbol) =>
        default(term)
    }
  }

  private val ListApply: RemoveApplyArgMutator =
    new RemoveApplyArgMutator(
      "ListApply",
      "List",
      Seq(
        "scala/collection/immutable/List.",
        "scala/package.List."
      ),
      minimum = 1
    )

  private val SeqApply: RemoveApplyArgMutator =
    new RemoveApplyArgMutator(
      "SeqApply",
      "Seq",
      Seq(
        "scala/collection/Seq.",
        "scala/collection/mutable/Seq.",
        "scala/collection/immutable/Seq.",
        "scala/package.Seq."
      ),
      minimum = 1
    )

  private val SetApply: RemoveApplyArgMutator =
    new RemoveApplyArgMutator(
      "SetApply",
      "Set",
      Seq(
        "scala/Predef.Set.",
        "scala/collection/mutable/Set.",
        "scala/collection/immutable/Set.",
        "scala/package.Set."
      ),
      minimum = 2
    )

  private val ReverseSymbols: Seq[String] =
    Seq(
      "scala/collection/SeqLike#reverse().",
      "scala/collection/immutable/List#reverse().",
      "scala/collection/IndexedSeqOptimized#reverse().",
      "scala/collection/SeqOps#reverse().",
      "scala/collection/IndexedSeqOps#reverse().",
      "scala/collection/ArrayOps#reverse()."
    )

  private val Reverse: SimpleMutator =
    new SimpleMutator("Reverse") {
      override def getMutator(implicit doc: SemanticDocument): MutationResult = {
        case reverse @ Term.Select(term, Term.Name("reverse"))
            if SymbolMatcher.exact(ReverseSymbols: _*).matches(reverse.symbol) =>
          default(term)
      }
    }

  private val Drop: RemoveOpMutator =
    new RemoveOpMutator(
      "Drop",
      "drop",
      Seq(
        "scala/collection/StrictOptimizedLinearSeqOps#drop().",
        "scala/collection/IterableOps#drop().",
        "scala/collection/immutable/Vector#drop().",
        "scala/collection/ArrayOps#drop()."
      )
    )

  private val Take: RemoveOpMutator =
    new RemoveOpMutator(
      "Take",
      "take",
      Seq(
        "scala/collection/immutable/List#take().",
        "scala/collection/IterableOps#take().",
        "scala/collection/immutable/Vector#take().",
        "scala/collection/ArrayOps#take()."
      )
    )

  private val ReduceOption: SimpleMutator =
    new SimpleMutator("ReduceOption") {
      override def getMutator(implicit doc: SemanticDocument): MutationResult = {
        case reduceOption @ Term.Apply(Term.Select(_, Term.Name("reduceOption")), _)
            if SymbolMatcher
              .exact("scala/collection/IterableOnceOps#reduceOption().")
              .matches(reduceOption.symbol) =>
          default(Term.Name("None"))
      }
    }

  override val getSubMutators: List[Mutator] =
    List(
      ListApply,
      SeqApply,
      SetApply,
      Reverse,
      Drop,
      Take,
      ReduceOption
    )

}
