package test

object ScalaOption {

  val op: Option[String] = Some("string")
  val op1 = if (???) "" else op.getOrElse("")
  val op2 = if (???) op.forall(str => str.startsWith("str")) else op.exists(str => str.startsWith("str"))
  val op3 = if (???) op.exists(str => str.contains("ing")) else op.forall(str => str.contains("ing"))
  val op4 = op.map(str => str + "!")
  val op5 = if (???) op.nonEmpty else op.isDefined
  val op6 = if (???) op.nonEmpty else op.isEmpty
  val op7 = if (???) op.isEmpty else op.nonEmpty
  val op8 = op.get
  val op9 = if (???) "" else op.fold("")(str => str * 3)
  val op10 = if (???) op else if (???) Some("test") else op.orElse(Some("test"))
  val op11 = if (???) null else op.orNull
  val op12 = if (???) op else if (???) op.filterNot(str => str.length < 3) else op.filter(str => str.length < 3)
  val op13 = if (???) op else if (???) op.filter(str => str.length >= 5) else op.filterNot(str => str.length >= 5)
  val op14 = if (???) true else if (???) false else op.contains("test")

}
