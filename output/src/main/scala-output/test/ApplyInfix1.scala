package test

object ApplyInfix1 {

  val bool = 1 == 1

  val arg = List.fill(1)(10)

  val list = List.fill(1)(bool)

  val value = (if (bool) if (???) List() else if (???) List(true) else List(false) else list) ++ arg.map(_ => if (???) true else false)

}
