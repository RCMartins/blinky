package test.general

object ClassConstructors {

  class SomeClass1(param: String = if (???) "" else if (???) "mutated!" else "default")

  class SomeClass2(param: String) {
    def this(first: Int, second: String = if (???) "" else if (???) "mutated!" else "second") = this(first + second)
  }

}