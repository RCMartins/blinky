package mutators

import play.api.libs.json.{Json, OWrites}

import scala.meta.Term

case class Mutation(id: Int, diffLines: List[String], original: Term, mutated: Term, mutationType: String = "")

object Mutation {
  implicit val jsonWrites: OWrites[Mutation] =
    (mutation: Mutation) => Json.obj(
      "id" -> mutation.id.toString,
      "diff" -> mutation.diffLines,
      "original" -> mutation.original.syntax,
      "mutated" -> mutation.mutated.syntax
    )
}
