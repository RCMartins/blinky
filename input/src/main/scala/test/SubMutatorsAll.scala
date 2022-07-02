/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [
  { ArithmeticOperators = [CharPlusToMinus, CharMinusToPlus, CharMulToDiv, CharDivToMul] }
  { ConditionalExpressions = [AndToOr, OrToAnd, RemoveUnaryNot] }
  { LiteralStrings = [EmptyToMutated, EmptyInterToMutated, NonEmptyToMutated,
                      NonEmptyInterToMutated] }
  { ScalaOptions = [GetOrElse, Exists, Forall, IsEmpty, NonEmpty, Fold,
                    OrElse, OrNull, Filter, FilterNot, Contains] }
  { ScalaTry = [GetOrElse, OrElse] }
  { Collections = [ListApply, SeqApply, SetApply] }
  { PartialFunctions = [RemoveOneCase] }
  { ScalaStrings = [Concat, Trim, ToUpperCase, ToLowerCase, Capitalize, StripPrefix,
                    StripSuffix, Map, FlatMap, DropWhile, TakeWhile] }
  { ControlFlow = [If] }
]
 */
package test

object SubMutatorsAll {}
