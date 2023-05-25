/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = LiteralStrings
 */
package test.general

object ClassConstructors {

  class SomeClass1(param: String = "default")

  class SomeClass2(param: String) {
    def this(first: Int, second: String = "second") = this(first + second)
  }

}
