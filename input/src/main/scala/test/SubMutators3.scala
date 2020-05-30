/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [
  { ConditionalExpressions = [AndToOr, OrToAnd, RemoveUnaryNot] }
  { LiteralStrings = [EmptyToMutated, EmptyInterToMutated, NonEmptyToMutated,
                      NonEmptyInterToMutated, ConcatToMutated] }
  { ScalaOptions = [GetOrElse, Exists, Forall, IsEmpty, NonEmpty, Fold,
                    OrElse, OrNull, Filter, FilterNot, Contains] }
  { ScalaTry = [GetOrElse, OrElse] }
  { Collections = [ListApply, SeqApply, SetApply] }
]
 */
package test

object SubMutators3 {}
