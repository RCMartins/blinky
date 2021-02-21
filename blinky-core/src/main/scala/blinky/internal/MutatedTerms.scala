package blinky.internal

import scala.meta.Term

trait MutatedTerms {}

object MutatedTerms {

  case class StandardMutatedTerms(mutated: Seq[Term], needsParens: Boolean) extends MutatedTerms

  case class PlaceholderMutatedTerms(
      placeholderFunction: Term => Term,
      mutated: Seq[(Term, Term)],
      newVars: Seq[Term.Name],
      needsParens: Boolean
  ) extends MutatedTerms

}
