package blinky.run

import zio.test._

object UtilsTest extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, TestFailure[Nothing]] =
    suite("Utils")(
      test("red should return the terminal color code for red color") {
        assertTrue(Utils.red("test line") == "\u001B[31mtest line\u001B[0m")
      },
      test("green should return the terminal color code for green color") {
        assertTrue(Utils.green("some line") == "\u001B[32msome line\u001B[0m")
      },
      test("cyan should return the terminal color code for cyan color") {
        assertTrue(Utils.cyan("hello world") == "\u001B[36mhello world\u001B[0m")
      },
      test("escapeString should return the string escaped") {
        assertTrue(Utils.escapeString("\"test line\"") == "\\\"test line\\\"")
      },
    )

}
