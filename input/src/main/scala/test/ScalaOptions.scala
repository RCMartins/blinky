/*
rule = MutateCode
MutateCode.enabledMutators = [ScalaOptions]
 */
package test

object ScalaOptions {

  val op: Option[String] = Some("string")
  val op1 = op.getOrElse("")
  val op2 = op.exists(str => str.startsWith("str"))
  val op3 = op.forall(str => str.contains("ing"))
  val op4 = op.map(str => str + "!")
  val op5 = op.isEmpty
  val op6 = op.isDefined
  val op7 = op.nonEmpty
  val op8 = op.get
  val op9 = op.fold("")(str => str * 3)
  val op10 = op.orElse(Some("test"))
  val op11 = op.orNull
  val op12 = op.filter(str => str.length < 3)
  val op13 = op.filterNot(str => str.length >= 5)
  val op14 = op.contains("test")

}
