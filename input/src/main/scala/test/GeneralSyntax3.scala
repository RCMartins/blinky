/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ArithmeticOperators, LiteralBooleans, Collections]
 */
package test

object GeneralSyntax3 {
  class Foo(value: Boolean)

  val foo1 = new Foo(true)

  val foo2 = new Foo(value = true)

  val foo3 = new Foo(false) {
    val bar = 1 + 1
  }

  class FooT[A](value: A)

  val foo4 = new FooT[Int](10 + 2)

  def fun[A](value: A): List[A] = List(value)

  val call1 = fun[Int](1 + 2)

  val call2 = fun(2 + 3: Int)

  def fun2(args: Boolean*): Boolean = args.forall(identity)

  val call3 = fun2(Seq(true, false): _*)
}
