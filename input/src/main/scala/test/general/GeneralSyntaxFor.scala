/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ConditionalExpressions, ArithmeticOperators]
 */
package test.general

object GeneralSyntaxFor {
  for {
    case (a, b) <- List((1, 2), (1 + 2, 4))
    c = a + b
    x <- 1 to 50 + 50
    if !(x % 7 < 3)
  } println(x)

  for {
    case (a, b) <- List((1, 2), (1 + 2, 4))
    c = a + b
    x <- 1 to 50 + 50
    if !(x % 7 < 3)
  } yield x
}
