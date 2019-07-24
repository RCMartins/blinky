import scala.meta.Term

package object mutators {

  implicit class TermToMutationTerm(terms: Seq[Term]) {
    def toMutation(needsParens: Boolean): MutatedTerms = MutatedTerms(terms, needsParens)
  }

}
