package blinky

import scala.meta.Term

package object internal {
  implicit class TermsToMutatedTerm(terms: Seq[Term]) {
    def toMutated(needsParens: Boolean): MutatedTerms = MutatedTerms(terms, needsParens)
  }
}
