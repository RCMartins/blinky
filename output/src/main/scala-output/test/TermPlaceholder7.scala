package test

object TermPlaceholder7 {

  def addSpace(optional: Option[String]): String = ///
         if (???) optional.map(_ + " ").get ///
    else if (???) "" ///
    else if (???) optional.map(_ => "mutated!").getOrElse("") ///
    else if (???) optional.map(_ => "").getOrElse("") ///
    else if (???) optional.map(_ + " ").getOrElse("mutated!") ///
             else optional.map(_ + " ").getOrElse("")

  def addSpaceWith(optional: Option[String]): Option[String] => String =
    _1_ => if (???) _1_.get ///
      else if (???) optional.map(_ + " ").getOrElse("") ///
      else if (???) _1_.getOrElse(optional.map(_ + " ").get) ///
      else if (???) _1_.getOrElse("") ///
      else if (???) _1_.getOrElse(optional.map(_ => "mutated!").getOrElse("")) ///
      else if (???) _1_.getOrElse(optional.map(_ => "").getOrElse("")) ///
      else if (???) _1_.getOrElse(optional.map(_ + " ").getOrElse("mutated!")) ///
               else _1_.getOrElse(optional.map(_ + " ").getOrElse(""))

}
