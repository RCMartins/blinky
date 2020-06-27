import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExampleTest extends AnyWordSpec with Matchers {
  "Example" must {
    "return big for the number 6" in {
      Example.calc(Some(6)) mustEqual "big"
    }

    "return small for the number 4" in {
      Example.calc(Some(4)) mustEqual "small"
    }

    "return an answer when the input is None" in {
      assert(Example.calc(None).nonEmpty)
    }
  }
}
