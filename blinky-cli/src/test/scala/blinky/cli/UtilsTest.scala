package blinky.cli

import blinky.run.Utils

class UtilsTest extends TestSpec {

  "red" should {
    "return the terminal color code for red color" in {
      Utils.red("test line") mustEqual "\u001B[31mtest line\u001B[0m"
    }
  }

  "green" should {
    "return the terminal color code for green color" in {
      Utils.green("some line") mustEqual "\u001B[32msome line\u001B[0m"
    }
  }

  "cyan" should {
    "return the terminal color code for cyan color" in {
      Utils.cyan("hello world") mustEqual "\u001B[36mhello world\u001B[0m"
    }
  }

  "escapeString" should {
    "return the string escaped" in {
      Utils.escapeString("\"test line\"") mustEqual "\\\"test line\\\""
    }
  }

  "prettyDiff" should {

    def testPrettyDiff(diffLinesStr: String, filePath: String, color: Boolean): String =
      Utils.prettyDiff(
        diffLinesStr
          .split("\n")
          .map {
            case ""   => " "
            case line => line
          }
          .mkString("\n"),
        filePath,
        color
      )

    "return the raw 'git diff' output with line numbers with color on" in {
      val original =
        """/home/user/blinky/input/src/main/scala/test/GeneralSyntax4.scala
          |@@ -8,7 +8,7 @@ package test
          | object GeneralSyntax4 {
          |   case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)
          |
          |-  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)
          |+  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 - 4).value1)
          |
          |   val some1 = Some("value")
          |""".stripMargin

      val actual =
        testPrettyDiff(
          original,
          "/home/user/blinky",
          color = true
        ).replace("\r", "")

      val expected =
        """/input/src/main/scala/test/GeneralSyntax4.scala
          |$[36m@@ -8,7 +8,7 @@ package test$[0m
          |   8        object GeneralSyntax4 {
          |   9          case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)
          |  10########
          |  11       $[31m-  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)$[0m
          |      11   $[32m+  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 - 4).value1)$[0m
          |  12  12####
          |  13  13      val some1 = Some("value")
          |""".stripMargin
          .replace("#", " ")
          .replace("$", "\u001B")
          .replace("\r", "")

      actual mustEqual expected
    }

    "return the raw 'git diff' output with line numbers with color off" in {
      val original =
        """/home/user/blinky/input/src/main/scala/test/ScalaOptions.scala
          |@@ -9,7 +9,7 @@ object ScalaOptions {
          |   val op: Option[String] = Some("string")
          |   val op1 = op.getOrElse("")
          |   val op2 = op.exists(str => str.startsWith("str"))
          |-  val op3 = op.forall(str => str.contains("ing"))
          |+  val op3 = op.exists(str => str.contains("ing"))
          |   val op4 = op.map(str => str + "!")
          |   val op5 = op.isEmpty
          |   val op6 = op.isDefined
          |""".stripMargin

      val actual =
        testPrettyDiff(
          original,
          "/home/user/blinky",
          color = false
        ).replace("\r", "")

      val expected =
        """/input/src/main/scala/test/ScalaOptions.scala
          |@@ -9,7 +9,7 @@ object ScalaOptions {
          |   9          val op: Option[String] = Some("string")
          |  10          val op1 = op.getOrElse("")
          |  11          val op2 = op.exists(str => str.startsWith("str"))
          |  12       -  val op3 = op.forall(str => str.contains("ing"))
          |      12   +  val op3 = op.exists(str => str.contains("ing"))
          |  13  13      val op4 = op.map(str => str + "!")
          |  14  14      val op5 = op.isEmpty
          |  15  15      val op6 = op.isDefined
          |""".stripMargin
          .replace("#", " ")
          .replace("\r", "")

      actual mustEqual expected
    }

  }

}
