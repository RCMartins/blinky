package blinky

import blinky.internal.MutatedTerms.StandardMutatedTerms

import scala.meta.Term

package object internal {
  implicit class TermsToMutatedTerm(terms: Seq[Term]) {
    def toMutated(needsParens: Boolean): StandardMutatedTerms =
      StandardMutatedTerms(terms, needsParens)
  }
}
