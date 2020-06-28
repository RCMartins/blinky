import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExampleTest extends AnyWordSpec with Matchers {

  "func1" must {
    "return 20" in {
      Example.getId mustEqual "20"
    }
  }

}
