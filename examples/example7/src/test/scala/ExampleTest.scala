import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import Example._

class ExampleTest extends AnyWordSpec with Matchers {

  "improve" must {
    "return foobar" in {
      Example.show(List(Number)) mustEqual "[<number>]"
    }

    "return <empty>" in {
      Example.show(List(String, Number, String)) mustEqual "[<string>, <number>, <string>]"
    }
  }

}
