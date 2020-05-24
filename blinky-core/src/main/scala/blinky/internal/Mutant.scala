package blinky.internal

import play.api.libs.json.{Json, OWrites}

import scala.meta.Term

case class Mutant(
    id: Int,
    diff: String,
    original: Term,
    mutated: Term
)

object Mutant {
  implicit val jsonWrites: OWrites[Mutant] =
    (mutant: Mutant) =>
      Json.obj(
        "id" -> mutant.id,
        "diff" -> mutant.diff,
        "original" -> mutant.original.syntax,
        "mutated" -> mutant.mutated.syntax
      )
}
