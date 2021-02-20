import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExampleTest extends AnyWordSpec with Matchers {
  "Example" must {
    "test that stuff happens" in {
      Example.set2 mustEqual Set("")
    }
  }
}
