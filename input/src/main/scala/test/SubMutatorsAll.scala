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
  { Collections = [ListApply, SeqApply, SetApply, Reverse, Drop, Take, ReduceOption, Prepend] }
  { PartialFunctions = [RemoveOneCase] }
  { ScalaStrings = [Concat, Trim, ToUpperCase, ToLowerCase, Capitalize, StripPrefix,
                    StripSuffix, Map, FlatMap, Drop, Take, DropWhile, TakeWhile, Reverse] }
  { ControlFlow = [If] }
  { ZIO = [When, Unless] }
]
 */
package test

object SubMutatorsAll {}
