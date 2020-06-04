package test

object PartialFunctionsMutators2 {
  case class Foo(str: String, bool: Boolean)

  ('a' to 'z').toList.collect (if (???) {
  case c @ 'a' => c
  case 'i' | 'o' => '#'
} else if (???) {
  case c @ 'e' => c
  case 'i' | 'o' => '#'
} else if (???) {
  case c @ ('a' | 'e') => c
  case 'i' => '#'
} else if (???) {
  case c @ ('a' | 'e') => c
  case 'o' => '#'
} else {
  case c @ ('a' | 'e') => c
  case 'i' | 'o' => '#'
})

  List(Foo("bar", true)).collect (if (???) {
  case Foo("abc", _) => 123
} else if (???) {
  case Foo("bar", _) => 123
} else {
  case Foo("abc" | "bar", _) => 123
})
}
