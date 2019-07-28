/*
rule = MutateCode
MutateCode.activeMutators = [ScalaOption, ArithmeticOperators]
 */
package test

object GeneralSyntax4 {

  case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)

  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)

  val some1 = Some("value")

  val pair1 = "str" -> some1.orNull[String]

}
