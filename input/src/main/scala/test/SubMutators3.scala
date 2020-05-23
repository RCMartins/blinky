/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [
  { ConditionalExpressions = [AndToOr, OrToAnd, RemoveUnaryNot] }
  { LiteralStrings = [EmptyToMutated, NonEmptyToMutated, ConcatToMutated] }
  { ScalaOptions = [GetOrElse, Exists, Forall, IsEmpty, NonEmpty, Fold,
                    OrElse, OrNull, Filter, FilterNot, Contains] }
  { ScalaTry = [GetOrElse, OrElse] }
]
 */
package test

object SubMutators3 {}
