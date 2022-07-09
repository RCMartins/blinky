package test

object CollectionsDropTake {
  val col1a = if (???) List("a", "b") else List("a", "b").take(5)
  val col1b = if (???) List("a", "b") else List("a", "b").drop(3)

  val col2a = if (???) Seq(1, 2) else Seq(1, 2).take(5)
  val col2b = if (???) Seq(1, 2) else Seq(1, 2).drop(3)

  val col3a = if (???) Vector('a', 'b') else Vector('a', 'b').take(5)
  val col3b = if (???) Vector('a', 'b') else Vector('a', 'b').drop(3)

  val col4a = if (???) Array(true, false) else Array(true, false).take(5)
  val col4b = if (???) Array(true, false) else Array(true, false).drop(3)

}
