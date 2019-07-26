package test

object ScalaOption {

  val op: Option[String] = Some("string")
  val op1 = if (???) "" else op.getOrElse("")
  val op2 = if (???) op.forall(_.startsWith("str")) else op.exists(_.startsWith("str"))
  val op3 = if (???) op.exists(_.contains("ing")) else op.forall(_.contains("ing"))
  val op4 = op.map(_ + "!")
  val op5 = if (???) op.nonEmpty else op.isDefined
  val op6 = if (???) op.nonEmpty else op.isEmpty
  val op7 = if (???) op.isEmpty else op.nonEmpty
  val op8 = op.get
  val op9 = if (???) "" else op.fold("")(_ * 3)
  val op10 = if (???) op else if (???) Some("test") else op.orElse(Some("test"))
  val op11 = if (???) null else op.orNull
  val op12 = if (???) op else if (???) op.filterNot(_.length < 3) else op.filter(_.length < 3)
  val op13 = if (???) op else if (???) op.filter(_.length >= 5) else op.filterNot(_.length >= 5)
  val op14 = if (???) true else if (???) false else op.contains("test")

}
