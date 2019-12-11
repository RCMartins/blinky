/*
rule = Blinky
Blinky.enabledMutators = [ScalaTry]
 */
package test

import scala.util.Try

object ScalaTry {
  val op: Try[String] = Try("try")
  val op1 = op.getOrElse("default")
  val op2 = op.orElse(Try("other"))
}
