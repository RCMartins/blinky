package test

object TermPlaceholder2 {

  val list1 = List(1, 2, 3).map(_1_ => if (???) _1_ - 10 else _1_ + 10)

  val list2 = List(Some(40)).map(_2_ => if (???) _2_.map(identity).get ///
                                   else if (???) 100 ///
                                            else _2_.map(identity).getOrElse(100))

  val list3 = List(Some(40)).map(_3_ => if (???) _3_.map(_ * 2).get ///
                                   else if (???) 200 ///
                                   else if (???) _3_.map(_ / 2).getOrElse(200) ///
                                            else _3_.map(_ * 2).getOrElse(200))

//  val list4 = Some(List[Boolean]().map(!_)).getOrElse(List.empty)
//
//  //ignore placeholder issues:
//  val concat2: String => String = "test" + _

}
