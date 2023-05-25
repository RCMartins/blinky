package blinky.internal

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class MutantFile(
    id: Int,
    diff: String,
    fileName: String,
    original: String,
    mutated: String
)

object MutantFile {
  implicit val encoder: JsonEncoder[MutantFile] = DeriveJsonEncoder.gen[MutantFile]
  implicit val decoder: JsonDecoder[MutantFile] = DeriveJsonDecoder.gen[MutantFile]
}
