package blinky.v0.mutators

import blinky.v0.Mutator.MutationResult
import blinky.v0.ReplaceType.NeedsParens
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.SemanticDocument

import scala.annotation.tailrec
import scala.meta.{Case, Pat, Term}

object PartialFunctions extends MutatorGroup {
  override val groupName: String = "PartialFunctions"

  override val getSubMutators: List[Mutator] =
    List(
      RemoveOneCase,
      RemoveOneAlternative
    )

  private object RemoveOneCase extends SimpleMutator("RemoveOneCase") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Term.PartialFunction(cases) if cases.lengthCompare(2) >= 0 =>
        @tailrec
        def removeOneCase(
            before: List[Case],
            terms: List[Case],
            result: List[List[Case]]
        ): List[List[Case]] =
          terms match {
            case Nil =>
              result
            case caseTerm :: others =>
              removeOneCase(before :+ caseTerm, others, (before ++ others) :: result)
          }

        NeedsParens(removeOneCase(Nil, cases, Nil).reverse.map(Term.PartialFunction(_)))
    }
  }

  private object RemoveOneAlternative extends SimpleMutator("RemoveOneAlternative") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Term.PartialFunction(cases) =>
        def findAlternatives(mainPat: Pat): List[Pat] =
          mainPat match {
            case Pat.Bind(name, pat) =>
              findAlternatives(pat).map(Pat.Bind(name, _))
            case Pat.Extract.After_4_6_0(term, pats) =>
              pats.zipWithIndex
                .flatMap { case (pat, index) => findAlternatives(pat).map((_, index)) }
                .map { case (mutated, index) => Pat.Extract.After_4_6_0(term, pats.updated(index, mutated)) }
            case Pat.Alternative(pat1, pat2) =>
              findAlternatives(pat1) ++ findAlternatives(pat2)
            case pat =>
              List(pat)
          }

        @tailrec
        def changeOneCase(
            before: List[Case],
            terms: List[Case],
            result: List[List[Case]]
        ): List[List[Case]] =
          terms match {
            case Nil =>
              result
            case caseTerm :: others =>
              val alternatives =
                findAlternatives(caseTerm.pat)
                  .filterNot(_.structure == caseTerm.pat.structure)

              val caseTermsMutated =
                alternatives
                  .map(pat => caseTerm.copy(pat = pat))
                  .reverse

              changeOneCase(
                before :+ caseTerm,
                others,
                caseTermsMutated.map(term => (before :+ term) ++ others) ++ result
              )
          }

        NeedsParens(changeOneCase(Nil, cases, Nil).reverse.map(Term.PartialFunction(_)))
    }
  }

}
