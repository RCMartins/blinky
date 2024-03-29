/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [LiteralBooleans]
 */
package test.general

object GeneralSyntax1 {
  case class Foo(bool: Boolean = true)

  def validate(bool: Boolean = true): Boolean = !bool

  val list = List(true, false)

  val pair = (true, true)

  val mat = (1, 2) match {
    case (1, 2) => true
    case (2, 1) => true
    case _      => false
  }

  val partial = list.collect { case true =>
    false
  }

  val list2 = list.map(_ => true)

  val callWithNamedParams = validate(bool = false)
}
