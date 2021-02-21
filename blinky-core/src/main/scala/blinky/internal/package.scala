package blinky

import scala.meta.Term

package object internal {

  implicit class TermsToMutatedTerm(terms: Seq[Term]) {
    def toMutated(needsParens: Boolean): MutatedTerms = MutatedTerms(terms, needsParens)
  }

  def syntaxParens(term: Term, needsParens: Boolean): String =
    term match {
      case _: Term.Apply =>
        term.syntax
      case _ if !needsParens =>
        term.syntax
      case _ =>
        "(" + term.syntax + ")"
    }

}
