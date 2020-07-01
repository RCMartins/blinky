/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [
  { ConditionalExpressions = [AndToOr, OrToAnd, RemoveUnaryNot] }
  { LiteralStrings = [EmptyToMutated, EmptyInterToMutated, NonEmptyToMutated,
                      NonEmptyInterToMutated] }
  { ScalaOptions = [GetOrElse, Exists, Forall, IsEmpty, NonEmpty, Fold,
                    OrElse, OrNull, Filter, FilterNot, Contains] }
  { ScalaTry = [GetOrElse, OrElse] }
  { Collections = [ListApply, SeqApply, SetApply] }
  { PartialFunctions = [RemoveOneCase] }
  { ScalaStrings = [Concat, Trim, ToUpperCase, ToLowerCase] }
]
 */
package test

object SubMutators3 {}
