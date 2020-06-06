package blinky.runZIO

sealed trait Instruction[+A, +P]

object Instruction {

  final case class Result[A, P](value: A) extends Instruction[A, P]

  final case class PrintLine[A, P](line: String, rest: Instruction[A, P]) extends Instruction[A, P]

  final case class PrintErrorLine[A, P](line: String, rest: Instruction[A, P])
      extends Instruction[A, P]

  final case class ExternalRunSync[A, P](
      op: String,
      args: Seq[String],
      path: P,
      rest: Instruction[A, P]
  ) extends Instruction[A, P]

}
