/*
rule = MutateCode
*/
package test

object ConditionalExpression1 {

  val bool1 = true
  val bool2 = false
  val bool3 = bool1 && bool2
  val bool4 = bool3 || bool2
  val bool5 = !bool4

}
