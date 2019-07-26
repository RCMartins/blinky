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

}
