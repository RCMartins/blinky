package test

object CollectionsPrepend {
  val list1 = if (???) List(2, 3) else 1 :: List(2, 3)
  val list2 = if (???) list1 else 0 :: list1
  val list3 = if (???) list1 else list1.prepended(2)

  val seq1 = if (???) Seq(1) else Seq(1).prepended(2)
  val seq2 = if (???) scala.Seq(1) else scala.Seq(1).prepended(2)
  val seq3 = if (???) scala.collection.mutable.Seq(1) else scala.collection.mutable.Seq(1).prepended(2)
  val seq4 = if (???) scala.collection.immutable.Seq(1) else scala.collection.immutable.Seq(1).prepended(2)

  val vec = if (???) Vector(1) else Vector(1).prepended(2)

  val arr = if (???) Array(1) else Array(1).prepended(2)
}
