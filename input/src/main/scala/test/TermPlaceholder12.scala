/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaOptions, ScalaStrings, Collections]
 */
package test

object TermPlaceholder12 {

  implicit class StringExtensions(initial: String) {
    def rep(str1: String, str2: String): String = initial.replace(str1, str2)
  }

  def result1: String => String = _.trim.rep("abc", "def")

  def result2: (String, String) => String = _.trim.rep(_, "def")

  def result3: (String, String) => String = _.trim.rep("abc", _)

}
