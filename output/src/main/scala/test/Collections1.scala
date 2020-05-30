package test

object Collections1 {
  val seq1 = if (???) Seq(2) else if (???) Seq(1) else Seq(1, 2)
  val seq2 = if (???) scala.collection.mutable.Seq(2) ///
        else if (???) scala.collection.mutable.Seq(1) ///
                 else scala.collection.mutable.Seq(1, 2)
  val seq3 = if (???) scala.collection.immutable.Seq(2) ///
        else if (???) scala.collection.immutable.Seq(1) ///
                 else scala.collection.immutable.Seq(1, 2)

  val list1 = if (???) List("b") else if (???) List("a") else List("a", "b")
  val list2 = if (???) scala.List("b") else if (???) scala.List("a") else scala.List("a", "b")
  val list3 = if (???) scala.collection.immutable.List("b") ///
         else if (???) scala.collection.immutable.List("a") ///
                  else scala.collection.immutable.List("a", "b")
}
