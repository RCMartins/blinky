package blinky.internal

import play.api.libs.json.{Json, OWrites}

import scala.meta.Term

case class Mutant(
    id: Int,
    diff: String,
    fileName: String,
    original: Term,
    mutated: Term,
    needsParens: Boolean
)

object Mutant {
  implicit val jsonWrites: OWrites[Mutant] =
    (mutant: Mutant) =>
      Json.obj(
        "id" -> mutant.id,
        "diff" -> mutant.diff,
        "fileName" -> mutant.fileName,
        "original" -> mutant.original.syntax,
        "mutated" -> syntaxParens(mutant.mutated, mutant.needsParens)
      )
}
