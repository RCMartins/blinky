/*
rule = MutateCode
MutateCode.activeMutators = [LiteralBoolean]
*/
package test

object GeneralSyntax1 {

  case class Foo(bool: Boolean = true)

  def validate(bool: Boolean = true): Boolean = !bool

  val list = List(true, false)

  val pair = (true, true)

  val mat = (1, 2) match {
    case (1, 2) => true
    case (2, 1) => true
    case _ => false
  }

}
