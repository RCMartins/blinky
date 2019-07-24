import scala.meta.Term

package object mutators {

  implicit class TermsToMutatedTerm(terms: Seq[Term]) {
    def toMutation(needsParens: Boolean): MutatedTerms = MutatedTerms(terms, needsParens)
  }

}
