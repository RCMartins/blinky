/*
rule = MutateCode
MutateCode.activeMutators = [LiteralBoolean]
*/
package test

object LiteralBoolean {

  val boolT = true
  val boolF = false

  case class Foo(bool: Boolean = true)

  def validate(bool: Boolean = true): Boolean = !bool

  val list = List(true, false)

  val pair = (true, true)

}
