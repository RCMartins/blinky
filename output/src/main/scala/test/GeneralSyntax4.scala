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

  val some1 = Some("value")

  val pair1 = "str" -> (if (???) null else some1.orNull[String])

  val applyType1 = if (???) Some(1 + 2).asInstanceOf[Option[String]] ///
              else if (???) Some(1 + 2).asInstanceOf[Option[String]].filterNot(_.nonEmpty) ///
              else if (???) Some(1 - 2).asInstanceOf[Option[String]].filter(_.nonEmpty) ///
                       else Some(1 + 2).asInstanceOf[Option[String]].filter(_.nonEmpty)

  val applyType2 = if (???) Some(3 + 4).asInstanceOf[Option[String]].isEmpty ///
              else if (???) Some(3 - 4).asInstanceOf[Option[String]].nonEmpty ///
                       else Some(3 + 4).asInstanceOf[Option[String]].nonEmpty

}
