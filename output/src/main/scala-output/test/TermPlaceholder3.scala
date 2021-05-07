package test

object TermPlaceholder3 {

  val list1 = List(1, 2, 3).map(_1_ => if (???) _1_ - 10 else _1_ + 10)

  val list2 = List(Some(40)).map(_5_ => if (???) _5_.map(identity).get ///
                                   else if (???) 100 ///
                                            else _5_.map(identity).getOrElse(100))

  val list3 = List(Some(40)).map(_10_ => if (???) _10_.map(_ * 2).get ///
                                    else if (???) 200 ///
                                    else if (???) _10_.map(_ / 2).getOrElse(200) ///
                                             else _10_.map(_ * 2).getOrElse(200))

  val list4 = if (???) Some(List[Boolean]().map(!_)).get ///
         else if (???) List.empty ///
                  else Some(List[Boolean]().map(!_)).getOrElse(List.empty)

}
