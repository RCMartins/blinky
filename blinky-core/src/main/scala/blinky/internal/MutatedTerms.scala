package blinky.internal

import scala.meta.Term

case class MutatedTerms(mutated: Seq[Term], needsParens: Boolean)
