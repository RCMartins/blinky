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

  "stripPathPrefix" should {
    "return the original string if prefix is not found" in {
      Utils.stripPathPrefix("str/ing1", "strg") mustEqual "str/ing1"
    }

    "return the striped string if prefix is found" in {
      Utils.stripPathPrefix(
        "/home/project/src/main/scala/Code.scala",
        "/home/project"
      ) mustEqual "/src/main/scala/Code.scala"
    }
  }

  "prettyDiff" should {

    def testPrettyDiff(
        diffLinesStr: String,
        fileName: String,
        projectPath: String,
        color: Boolean
    ): String =
      Utils.prettyDiff(
        diffLinesStr
          .split("\n")
          .map {
            case ""   => " "
            case line => line
          }
          .mkString("\n"),
        fileName,
        projectPath,
        color
      )

    "return the raw 'git diff' output with line numbers (when color is on)" in {
      val original =
        """@@ -4,7 +4,7 @@ package test
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
          "/home/user/blinky/input/src/main/scala/test/GeneralSyntax4.scala",
          "/home/user/blinky",
          color = true
        ).replace("\r", "")

      val expected =
        """/input/src/main/scala/test/GeneralSyntax4.scala
          |$[36m@@ -4,7 +4,7 @@ package test$[0m
          |  4       object GeneralSyntax4 {
          |  5         case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)
          |  6#######
          |  7      $[31m-  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)$[0m
          |     7   $[32m+  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 - 4).value1)$[0m
          |  8  8####
          |  9  9      val some1 = Some("value")""".stripMargin
          .replace("#", " ")
          .replace("$", "\u001B")
          .replace("\r", "")

      actual mustEqual expected
    }

    "return the raw 'git diff' output with line numbers (when color is off)" in {
      val original =
        """@@ -14,8 +14,6 @@ package test
          | object SomeFile {
          |
          |   val value =
          |-    !Some(
          |-      true || false
          |-    ).get
          |+    !Some(true && false).get
          |
          | }""".stripMargin

      val actual =
        testPrettyDiff(
          original,
          "/home/user/scala-project/src/main/scala/SomeFile.scala",
          "/home/user/scala-project",
          color = false
        ).replace("\r", "")

      val expected =
        """/src/main/scala/SomeFile.scala
          |@@ -14,8 +14,6 @@ package test
          |  14        object SomeFile {
          |  15########
          |  16          val value =
          |  17       -    !Some(
          |  18       -      true || false
          |  19       -    ).get
          |      17   +    !Some(true && false).get
          |  20  18####
          |  21  19    }""".stripMargin
          .replace("#", " ")
          .replace("\r", "")

      actual mustEqual expected
    }

    "return the raw 'git diff' output with line numbers (multiple minus and plus lines)" in {
      val original =
        """@@ -10,7 +10,9 @@ object MatchSyntax1 {
          |   val value2: Option[Int] = Some(30)
          |   val value3 =
          |     (value1.fold(value2)(x => Some(x + 4)) match {
          |-      case None    => 2 + 3
          |-      case Some(v) => v
          |-    }) + 10
          |+  case None =>
          |+    2 + 3
          |+  case Some(v) =>
          |+    v
          |+}) - 10
          | }""".stripMargin

      val actual =
        testPrettyDiff(
          original,
          "/home/user/blinky/input/src/main/scala/test/MatchSyntax1.scala",
          "/home/user/blinky",
          color = false
        ).replace("\r", "")

      val expected =
        """/input/src/main/scala/test/MatchSyntax1.scala
          |@@ -10,7 +10,9 @@ object MatchSyntax1 {
          |  10          val value2: Option[Int] = Some(30)
          |  11          val value3 =
          |  12            (value1.fold(value2)(x => Some(x + 4)) match {
          |  13       -      case None    => 2 + 3
          |  14       -      case Some(v) => v
          |  15       -    }) + 10
          |      13   +  case None =>
          |      14   +    2 + 3
          |      15   +  case Some(v) =>
          |      16   +    v
          |      17   +}) - 10
          |  16  18    }""".stripMargin
          .replace("#", " ")
          .replace("\r", "")

      actual mustEqual expected
    }

  }

}
