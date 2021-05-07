/*
rule = Blinky
Blinky.mutantsOutputFile = ???
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ScalaStrings]
 */
package test

object TermPlaceholder2 {

  val concat: String => String = "test" + _

  def trimList(list: List[String]): List[String] = list.map(_.trim)

}
