import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExampleTest extends AnyWordSpec with Matchers {
  "Utils" must {
    "return number 6" in {
      Utils.Number mustEqual 6
    }
  }
}
