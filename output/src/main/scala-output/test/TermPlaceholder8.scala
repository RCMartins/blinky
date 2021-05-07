package test

object TermPlaceholder8 {

  def trimOpt(optional: Option[String]): String =
    if (???) optional.map(_.trim).get ///
else if (???) "" ///
else if (???) optional.map(identity).getOrElse("") ///
else if (???) optional.map(_.trim).getOrElse("mutated!") ///
         else optional.map(_.trim).getOrElse("")

  def func(text1: String, text2: String): String =
    text1.r.replaceAllIn(text2, _2_ => if (???) "mutated!" else if (???) "" else " " + _2_.group(0).map(_.toString))

  def trimOpt2(optional: Option[String]): String =
    if (???) (optional map (_.trim)).get ///
else if (???) "" ///
else if (???) (optional map identity).getOrElse("") ///
else if (???) (optional map (_.trim)).getOrElse("mutated!") ///
         else (optional map (_.trim)).getOrElse("")

  def trimOpt3(optional: Option[String]): String => String =
    _5_ => if (???) optional mkString _5_ else optional mkString _5_.trim

}
