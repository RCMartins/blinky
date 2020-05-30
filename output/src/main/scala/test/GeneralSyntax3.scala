package test

object GeneralSyntax3 {
  class Foo(value: Boolean)

  val foo1 = new Foo(if (???) false else true)

  val foo2 = new Foo(value = if (???) false else true)

  val foo3 = new Foo(if (???) true else false) {
    val bar = if (???) 1 - 1 else 1 + 1
  }

  class FooT[A](value: A)

  val foo4 = new FooT[Int](if (???) 10 - 2 else 10 + 2)

  def fun[A](value: A): List[A] = List(value)

  val call1 = fun[Int](if (???) 1 - 2 else 1 + 2)

  val call2 = fun((if (???) 2 - 3 else 2 + 3): Int)

  def fun2(args: Boolean*): Boolean = args.forall(identity)

  val call3 = fun2((if (???) Seq(false) ///
               else if (???) Seq(true) ///
               else if (???) Seq(false, false) ///
               else if (???) Seq(true, true) ///
                        else Seq(true, false)): _*)
}
