package test

object All1 {
  val op: Option[String] = if (???) Some("string").orElse(Some("test")) ///
                      else if (???) Some("string").orElse(Some("test")).filterNot(str => str.length < 3 * 2) ///
                      else if (???) Some("string").filter(str => str.length < 3 * 2) ///
                      else if (???) Some("test").filter(str => str.length < 3 * 2) ///
                      else if (???) Some("").orElse(Some("test")).filter(str => str.length < 3 * 2) ///
                      else if (???) Some("mutated!").orElse(Some("test")).filter(str => str.length < 3 * 2) ///
                      else if (???) Some("string").orElse(Some("")).filter(str => str.length < 3 * 2) ///
                      else if (???) Some("string").orElse(Some("mutated!")).filter(str => str.length < 3 * 2) ///
                      else if (???) Some("string").orElse(Some("test")).filter(str => str.length < 3 / 2) ///
                               else Some("string").orElse(Some("test")).filter(str => str.length < 3 * 2)
}
