import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExampleTest extends AnyWordSpec with Matchers {
  "Example" must {
    "return \"result\"" in {
      Example.calc(Some(20)) mustEqual "result"
    }
  }
}
