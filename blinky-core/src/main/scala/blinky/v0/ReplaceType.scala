package blinky.v0

import scala.meta.Term

sealed trait ReplaceType {

  def terms: List[Term]

  def fullReplace: Boolean

  def needsParens: Boolean

}

object ReplaceType {

  case class Standard(terms: List[Term]) extends ReplaceType {
    val fullReplace: Boolean = false
    val needsParens: Boolean = false
  }

  case class FullReplace(terms: List[Term]) extends ReplaceType {
    val fullReplace: Boolean = true
    val needsParens: Boolean = false
  }

  case class CaseCrazy(terms: List[Term]) extends ReplaceType {
    val fullReplace: Boolean = false
    val needsParens: Boolean = true
  }

}
