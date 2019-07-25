package test

object GeneralSyntax3 {

  class Foo(value: Boolean)

  val foo1 = new Foo(if (sys.props.contains("SCALA_MUTATION_1")) false else true)

  val foo2 = new Foo(value = if (sys.props.contains("SCALA_MUTATION_2")) false else true)

  val foo3 = new Foo(if (sys.props.contains("SCALA_MUTATION_3")) true else false) {
    val bar = if (sys.props.contains("SCALA_MUTATION_4")) 1 - 1 else 1 + 1
  }

  class FooT[A](value: A)

  val foo4 = new FooT[Int](if (sys.props.contains("SCALA_MUTATION_5")) 10 - 2 else 10 + 2)

  def fun[A](value: A): List[A] = List(value)

  val call1 = fun[Int](if (sys.props.contains("SCALA_MUTATION_6")) 1 - 2 else 1 + 2)

  val call2 = fun((if (sys.props.contains("SCALA_MUTATION_7")) 2 - 3 else 2 + 3): Int)

  def fun2(args: Boolean*): Boolean = args.forall(identity)

  val call3 = fun2(Seq(if (sys.props.contains("SCALA_MUTATION_8")) false else true, if (sys.props.contains("SCALA_MUTATION_9")) true else false): _*)

}
