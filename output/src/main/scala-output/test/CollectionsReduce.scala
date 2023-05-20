package test

object CollectionsReduce {
  val col1 = if (???) None else List("a", "b", "c").reduceOption((a, b) => a * 2 + b)

  val col2 = if (???) None else Seq[Int]().reduceOption((a, b) => a + b)
}
