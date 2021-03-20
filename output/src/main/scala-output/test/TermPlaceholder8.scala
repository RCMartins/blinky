package test

object TermPlaceholder8 {

  def trimOpt(optional: Option[String]): String =
    if (???) optional.map(_.trim).get ///
else if (???) "" ///
else if (???) optional.map(identity).getOrElse("") ///
else if (???) optional.map(_.trim).getOrElse("mutated!") ///
         else optional.map(_.trim).getOrElse("")

}
