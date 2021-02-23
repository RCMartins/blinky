package blinky

import blinky.internal.MutatedTerms.StandardMutatedTerms

import scala.meta.{Lit, Term}

package object internal {

  implicit class TermsToMutatedTerm(terms: Seq[Term]) {
    def toMutated(needsParens: Boolean): StandardMutatedTerms =
      StandardMutatedTerms(terms, needsParens)
  }

  def syntaxParens(term: Term, needsParens: Boolean): String =
    term match {
      case _: Lit | _: Term.Apply | _: Term.Name =>
        term.syntax
      case _ if !needsParens =>
        term.syntax
      case _ =>
        "(" + term.syntax + ")"
    }

}
