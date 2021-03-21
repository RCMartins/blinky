package test

object SpecificMutantIndices {
  val string1 = ""
  val string2 = if (_root_.scala.sys.env.contains("BLINKY_MUTATION_2")) "" else "a cool string"
  val string4 = if (_root_.scala.sys.env.contains("BLINKY_MUTATION_4")) "" else "mutated!"
  val string5 = s""
  val string6 = if (_root_.scala.sys.env.contains("BLINKY_MUTATION_6")) "" else if (_root_.scala.sys.env.contains("BLINKY_MUTATION_7")) "mutated!" else s"$string2-test"
  val string7 = if (_root_.scala.sys.env.contains("BLINKY_MUTATION_8")) "" else s"$string2"
  val string8 = s"test"
  val string9 = if (_root_.scala.sys.env.contains("BLINKY_MUTATION_12")) "mutated!" else f""
  val string10 = Some(string2).map(_1_ => if (_root_.scala.sys.env.contains("BLINKY_MUTATION_14")) "" else _1_ + "!!")
}
