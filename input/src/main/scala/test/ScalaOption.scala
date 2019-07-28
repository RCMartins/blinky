/*
rule = MutateCode
MutateCode.activeMutators = [ScalaOption]
 */
package test

object ScalaOption {

  val op: Option[String] = Some("string")
  val op1 = op.getOrElse("")
  val op2 = op.exists(_.startsWith("str"))
  val op3 = op.forall(_.contains("ing"))
  val op4 = op.map(_ + "!")
  val op5 = op.isDefined
  val op6 = op.isEmpty
  val op7 = op.nonEmpty
  val op8 = op.get
  val op9 = op.fold("")(_ * 3)
  val op10 = op.orElse(Some("test"))
  val op11 = op.orNull
  val op12 = op.filter(_.length < 3)
  val op13 = op.filterNot(_.length >= 5)
  val op14 = op.contains("test")

}
