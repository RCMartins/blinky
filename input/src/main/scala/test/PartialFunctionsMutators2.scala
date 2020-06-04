/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [PartialFunctions.RemoveOneAlternative]
 */
package test

object PartialFunctionsMutators2 {
  case class Foo(str: String, bool: Boolean)

  ('a' to 'z').toList.collect {
    case c @ ('a' | 'e') => c
    case 'i' | 'o'       => '#'
  }

  List(Foo("bar", true)).collect {
    case Foo("abc" | "bar", _) => 123
  }
}
