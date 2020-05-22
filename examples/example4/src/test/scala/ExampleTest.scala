import org.scalatest.FunSuite

class ExampleTest extends FunSuite {
  test("return big for the number 6") {
    assert(Example.calc(Some(6)) == "big")
  }

  test("return small for the number 4") {
    assert(Example.calc(Some(4)) == "small")
  }

  test("return '<empty>' when the input is None") {
    assert(Example.calc(None) == "<empty>")
  }
}
