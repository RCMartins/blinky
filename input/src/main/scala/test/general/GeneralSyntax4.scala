/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, ArithmeticOperators, Collections]
 */
package test.general

object GeneralSyntax4 {
  case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)

  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)

  val foo2 = Foo(1 + 1, 3)(4, 5)

  val foo3 = List(1, 2)(0)

  val some1 = Some("value")

  val pair1 = "str" -> some1.orNull[String]

  val applyType1 = Some(1 + 2).asInstanceOf[Option[Int]].filter(v => v == 3)

  val applyType2 = Some(3 + 4).asInstanceOf[Option[Int]].nonEmpty
}
