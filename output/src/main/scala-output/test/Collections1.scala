package test

object Collections1 {
  val list1 = if (???) List("b", "c", "d") ///
         else if (???) List("a", "c", "d") ///
         else if (???) List("a", "b", "d") ///
         else if (???) List("a", "b", "c") ///
                  else List("a", "b", "c", "d")
  val list2 = if (???) scala.List("b") else if (???) scala.List("a") else scala.List("a", "b")
  val list3 = if (???) scala.collection.immutable.List("b") ///
         else if (???) scala.collection.immutable.List("a") ///
                  else scala.collection.immutable.List("a", "b")
  val list4 = if (???) List() else List("foo")

  val seq1 = if (???) Seq(2) else if (???) Seq(1) else Seq(1, 2)
  val seq2 = if (???) scala.Seq(2) else if (???) scala.Seq(1) else scala.Seq(1, 2)
  val seq3 = if (???) scala.collection.mutable.Seq(2) ///
        else if (???) scala.collection.mutable.Seq(1) ///
                 else scala.collection.mutable.Seq(1, 2)
  val seq4 = if (???) scala.collection.immutable.Seq(2) ///
        else if (???) scala.collection.immutable.Seq(1) ///
                 else scala.collection.immutable.Seq(1, 2)

  val setEmpty = Set()
  val set1 = if (???) Set(2) else if (???) Set(1) else Set(1, 2)
  val set2 = if (???) scala.collection.mutable.Set(2) ///
        else if (???) scala.collection.mutable.Set(1) ///
                 else scala.collection.mutable.Set(1, 2)
  val set3 = if (???) scala.collection.immutable.Set(2) ///
        else if (???) scala.collection.immutable.Set(1) ///
                 else scala.collection.immutable.Set(1, 2)
  val set4 = Set(1)
}
