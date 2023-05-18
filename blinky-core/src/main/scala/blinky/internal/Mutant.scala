package blinky.internal

import scala.meta.Term

case class Mutant(
    id: Int,
    diff: String,
    fileName: String,
    original: Term,
    mutated: Term,
    needsParens: Boolean
) {

  def toMutantFile: MutantFile =
    MutantFile(
      id = id,
      diff = diff,
      fileName = fileName,
      original = original.syntax,
      mutated = syntaxParens(mutated, needsParens)
    )

}
