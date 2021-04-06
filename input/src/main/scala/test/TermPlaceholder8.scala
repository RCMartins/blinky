/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, LiteralStrings, ScalaStrings]
 */
package test

object TermPlaceholder8 {

  def trimOpt(optional: Option[String]): String =
    optional.map(_.trim).getOrElse("")

  def func(text1: String, text2: String): String =
    text1.r.replaceAllIn(text2, " " + _.group(0).map(_.toString))

  def trimOpt2(optional: Option[String]): String =
    (optional map (_.trim)).getOrElse("")

  def trimOpt3(optional: Option[String]): String => String =
    optional mkString _.trim

}
