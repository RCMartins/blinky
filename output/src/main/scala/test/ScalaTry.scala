package test

import scala.util.Try

object ScalaTry {
  val op: Try[String] = Try("try")
  val op1 = if (???) op.get else if (???) "default" else op.getOrElse("default")
  val op2 = if (???) op else if (???) Try("other") else op.orElse(Try("other"))
}
