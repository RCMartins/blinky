package blinky.run

import blinky.TestSpec
import zio.test.{Spec, TestFailure, TestSuccess, suite}
import zio.test.environment.TestEnvironment

import zio.test.Assertion._
import zio.test._

object UtilsTest extends TestSpec {

  val spec: Spec[TestEnvironment, TestFailure[Nothing], TestSuccess] =
    suite("Utils")(
      test("red should return the terminal color code for red color") {
        assert(Utils.red("test line"))(equalTo("\u001B[31mtest line\u001B[0m"))
      },
      test("green should return the terminal color code for green color") {
        assert(Utils.green("some line"))(equalTo("\u001B[32msome line\u001B[0m"))
      },
      test("cyan should return the terminal color code for cyan color") {
        assert(Utils.cyan("hello world"))(equalTo("\u001B[36mhello world\u001B[0m"))
      },
      test("escapeString should return the string escaped") {
        assert(Utils.escapeString("\"test line\""))(equalTo("\\\"test line\\\""))
      },
      suite("stripPathPrefix")(
        test("should return the original string if prefix is not found") {
          assert(Utils.stripPathPrefix("str/ing1", "strg"))(equalTo("str/ing1"))
        },
        test("should return the striped string if prefix is found") {
          assert(
            Utils.stripPathPrefix(
              "/home/project/src/main/scala/Code.scala",
              "/home/project"
            )
          )(equalTo("/src/main/scala/Code.scala"))
        }
      ), {
        def testPrettyDiff(
            diffLinesStr: String,
            fileName: String,
            projectPath: String,
            color: Boolean
        ): String =
          Utils.prettyDiff(
            diffLinesStr
              .replace("#", " ")
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

        suite("prettyDiff")(
          test("return the raw 'git diff' output with line numbers (when color is on)") {
            val original =
              """@@ -3,7 +3,7 @@ package test
                | object GeneralSyntax4 {
                |   case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)
                |
                |-  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)
                |+  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 - 4).value1)
                |
                |   val some1 = Some("value")
                |#""".stripMargin

            val actual =
              testPrettyDiff(
                original,
                "/home/user/blinky/input/src/main/scala/test/GeneralSyntax4.scala",
                "/home/user/blinky",
                color = true
              ).replace("\r", "")

            val expected =
              """/input/src/main/scala/test/GeneralSyntax4.scala
                |$[36m@@ -3,7 +3,7 @@ package test$[0m
                |  3       object GeneralSyntax4 {
                |  4         case class Foo(value1: Int, value2: Int)(value3: Int, value4: Int)
                |  5   ####
                |$[31m  6      -  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 + 4).value1)$[0m
                |$[32m     6   +  val foo1 = Some(2).contains(Foo(1 + 1, 2 + 2)(3 + 3, 4 - 4).value1)$[0m
                |  7  7####
                |  8  8      val some1 = Some("value")
                |  9  9   #""".stripMargin
                .replace("#", " ")
                .replace("$", "\u001B")
                .replace("\r", "")

            assert(actual)(equalTo(expected))
          },
          test("return the raw 'git diff' output with line numbers (when color is off)") {
            val original =
              """@@ -3,8 +3,6 @@ package test
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
                |@@ -3,8 +3,6 @@ package test
                |   3        object SomeFile {
                |   4########
                |   5          val value =
                |   6       -    !Some(
                |   7       -      true || false
                |   8       -    ).get
                |       6   +    !Some(true && false).get
                |   9   7####
                |  10   8    }""".stripMargin
                .replace("#", " ")
                .replace("\r", "")

            assert(actual)(equalTo(expected))
          },
          test(
            "return the raw 'git diff' output with line numbers (multiple minus and plus lines)"
          ) {
            val original =
              """@@ -92,7 +92,9 @@ object MatchSyntax1 {
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
                |@@ -92,7 +92,9 @@ object MatchSyntax1 {
                |   92           val value2: Option[Int] = Some(30)
                |   93           val value3 =
                |   94             (value1.fold(value2)(x => Some(x + 4)) match {
                |   95        -      case None    => 2 + 3
                |   96        -      case Some(v) => v
                |   97        -    }) + 10
                |        95   +  case None =>
                |        96   +    2 + 3
                |        97   +  case Some(v) =>
                |        98   +    v
                |        99   +}) - 10
                |   98  100    }""".stripMargin
                .replace("#", " ")
                .replace("\r", "")

            assert(actual)(equalTo(expected))
          },
          test("return the raw 'git diff' output with line numbers (only minus)") {
            val original =
              """@@ -75,7 +75,6 @@
                | foo
                | bar
                | baz
                |-line1
                | more context
                | and more
                | and still context""".stripMargin

            val actual =
              testPrettyDiff(
                original,
                "/home/user/project/someFile.scala",
                "/home/user/project",
                color = false
              ).replace("\r", "")

            val expected =
              """/someFile.scala
                |@@ -75,7 +75,6 @@
                |  75        foo
                |  76        bar
                |  77        baz
                |  78       -line1
                |  79  78    more context
                |  80  79    and more
                |  81  80    and still context""".stripMargin
                .replace("#", " ")
                .replace("\r", "")

            assert(actual)(equalTo(expected))
          },
          test("return the raw 'git diff' output with line numbers (only plus)") {
            val original =
              """@@ -75,6 +75,8 @@
                | foo
                | bar
                | baz
                |+line1
                |+line2
                | more context
                | and more
                | and still context""".stripMargin

            val actual =
              testPrettyDiff(
                original,
                "/home/user/project/someFile.scala",
                "/home/user/project",
                color = false
              ).replace("\r", "")

            val expected =
              """/someFile.scala
                |@@ -75,6 +75,8 @@
                |  75        foo
                |  76        bar
                |  77        baz
                |      78   +line1
                |      79   +line2
                |  78  80    more context
                |  79  81    and more
                |  80  82    and still context""".stripMargin
                .replace("#", " ")
                .replace("\r", "")

            assert(actual)(equalTo(expected))
          }
        )
      }
    )

}
