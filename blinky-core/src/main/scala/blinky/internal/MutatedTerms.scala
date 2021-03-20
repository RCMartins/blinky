package blinky.internal

import scala.meta.Term

sealed trait MutatedTerms {}

object MutatedTerms {

  case class StandardMutatedTerms(mutated: Seq[Term], needsParens: Boolean) extends MutatedTerms

  case class PlaceholderMutatedTerms(
      originalReplaced: Term,
      placeholderFunction: Term => Term,
      mutated: Seq[(Term, Term)],
      newVars: Seq[String],
      placeholderLocation: Option[Term],
      needsParens: Boolean
  ) extends MutatedTerms

}
