/*
rule = MutateCode
*/
package test

object All1 {

  val op: Option[String] = Some("string").orElse(Some("test")).filter(_.length < 3 * 2)

}
