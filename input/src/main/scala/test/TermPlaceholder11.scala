/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, ScalaStrings, Collections]
 */
package test

object TermPlaceholder11 {

  def useF(
      f: List[String] => List[String],
      list: List[String]
  ): List[String] = f(list)

  def value: List[String] => List[String] = useF(_.reverse, _).reverse

  implicit class StringExtensions(initial: String) {
    def !(str1: String, str2: String): String = initial.replace(str1, str2)
  }

  def result1: String => String = _.trim ! ("abc", "def")

  def result2: (String, String) => String = _.trim ! (_, "def")

  def result3: (String, String) => String = _.trim ! ("abc", _)

}
