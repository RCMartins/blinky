package test

object GeneralSyntax4 {
  case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)

  val foo1 = if (???) true ///
        else if (???) false ///
        else if (???) Some(2).contains(Foo(1 - 1, 2 + 2)(3 + 3, 4 + 4).value1) ///
        else if (???) Some(2).contains(Foo(1 + 1, 2 - 2)(3 + 3, 4 + 4).value1) ///
        else if (???) Some(2).contains(Foo(1 + 1, 2 + 2)(3 - 3, 4 + 4).value1) ///
        else if (???) Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 - 4).value1) ///
        else Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)

  val foo2 = Foo(if (???) 1 - 1 else 1 + 1, 3)(4, 5)

  val foo3 = (if (???) List(2) else if (???) List(1) else List(1, 2))(0)

  val some1 = Some("value")

  val pair1 = "str" -> (if (???) null else some1.orNull[String])

  val applyType1 = if (???) Some(1 + 2).asInstanceOf[Option[Int]] ///
              else if (???) Some(1 + 2).asInstanceOf[Option[Int]].filterNot(v => v == 3) ///
              else if (???) Some(1 - 2).asInstanceOf[Option[Int]].filter(v => v == 3) ///
                       else Some(1 + 2).asInstanceOf[Option[Int]].filter(v => v == 3)

  val applyType2 = if (???) Some(3 + 4).asInstanceOf[Option[Int]].isEmpty ///
              else if (???) Some(3 - 4).asInstanceOf[Option[Int]].nonEmpty ///
                       else Some(3 + 4).asInstanceOf[Option[Int]].nonEmpty
}
