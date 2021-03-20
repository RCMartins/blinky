package test

object TermPlaceholder8 {

  def trimOpt(optional: Option[String]): String =
    if (???) optional.map(_.trim).get ///
else if (???) "" ///
else if (???) optional.map(identity).getOrElse("") ///
else if (???) optional.map(_.trim).getOrElse("mutated!") ///
         else optional.map(_.trim).getOrElse("")

  def func(text1: String, text2: String): String =
    text1.r.replaceAllIn(text2, _1_ => if (???) "mutated!" else if (???) "" else " " + _1_.group(0).map(_.toString))

}
