package blinky.v0

import scala.meta.Term

sealed trait ReplaceType {

  def terms: Seq[Term]

  def fullReplace: Boolean

  def needsParens: Boolean

}

object ReplaceType {

  case class Standard(terms: Seq[Term]) extends ReplaceType {
    val fullReplace: Boolean = false
    val needsParens: Boolean = false
  }

  case class FullReplace(terms: Seq[Term]) extends ReplaceType {
    val fullReplace: Boolean = true
    val needsParens: Boolean = false
  }

  case class NeedsParens(terms: Seq[Term]) extends ReplaceType {
    val fullReplace: Boolean = false
    val needsParens: Boolean = true
  }

}
