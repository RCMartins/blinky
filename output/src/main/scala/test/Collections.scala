package test

object Collections {
  val seq1 = if (???) Seq(2) else if (???) Seq(1) else Seq(1, 2)
  val seq2 = if (???) scala.collection.mutable.Seq(2) ///
        else if (???) scala.collection.mutable.Seq(1) ///
                 else scala.collection.mutable.Seq(1, 2)
  val seq3 = if (???) scala.collection.immutable.Seq(2) ///
        else if (???) scala.collection.immutable.Seq(1) ///
                 else scala.collection.immutable.Seq(1, 2)
}
