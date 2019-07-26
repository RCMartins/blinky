package test

object GeneralSyntax1 {

  case class Foo(bool: Boolean = if (???) false else true)

  def validate(bool: Boolean = if (???) false else true): Boolean = !bool

  val list = List(if (???) false else true, if (???) true else false)

  val pair = (if (???) false else true, if (???) false else true)

  val mat = (1, 2) match {
    case (1, 2) => if (???) false else true
    case (2, 1) => if (???) false else true
    case _ => if (???) true else false
  }

  val partial = list.collect {
    case true => if (???) true else false
  }

  val list2 = list.map(_ => if (???) false else true)

  val callWithNamedParams = validate(bool = if (???) true else false)

}