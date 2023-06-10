---
id: mutators
title: Mutators
sidebar_label: Mutators
---

## Mutators

Mutators are the transformations to the code that we want to apply.
Because of several factors like time to run or importance we may want to enable/disable some available mutators.

## Available Mutators

* [Literal Booleans](#literal-booleans)
* [Arithmetic Operators](#arithmetic-operators)
* [Conditional Expressions](#conditional-expressions)
* [Literal Strings](#literal-strings)
* [Scala Options](#scala-options)
* [Scala Try](#scala-try)
* [Scala Collections](#scala-collections)
* [Partial Functions](#partial-functions)
* [Scala Strings](#scala-strings)
* [Control Flow](#control-flow)
* [ZIO](#zio)

### Literal Booleans

name: LiteralBooleans

description: changes true into false and false into true.  

example:

```diff
- val bool = true
+ val bool = false
```

---

### Arithmetic Operators

group name: ArithmeticOperators

#### Int - Plus into Minus

name: IntPlusToMinus

description: Changes the arithmetic operator `+` into `-` when operating on `Int` with `Int`.

example:

```diff
- val value = list.size + 5
+ val value = list.size - 5
```

(Note that it only applies to `Int` with `Int`)

#### Int - Minus into Plus

name: IntMinusToPlus

description: Changes the arithmetic operator `-` into `+` when operating on `Int` with `Int`.

#### Int - Multiply into Divide

name: IntMulToDiv

description: Changes the arithmetic operator `*` into `/` when operating on `Int` with `Int`.

#### Int - Divide into Multiply

name: IntDivToMul

description: Changes the arithmetic operator `/` into `*` when operating on `Int` with `Int`.

#### Char - Plus into Minus

name: CharPlusToMinus

description: Changes the arithmetic operator `+` into `-` when operating on `Char` with `Int`.

example:

```diff
- val value = 'J' + 5
+ val value = 'J' - 5
```

(Note that it only applies to `Char` with `Int`)

#### Char - Minus into Plus

name: CharMinusToPlus

description: Changes the arithmetic operator `-` into `+` when operating on `Char` with `Int`.

#### Char - Multiply into Divide

name: CharMulToDiv

description: Changes the arithmetic operator `*` into `/` when operating on `Char` with `Int`.

#### Char - Divide into Multiply

name: CharDivToMul

description: Changes the arithmetic operator `/` into `*` when operating on `Char` with `Int`.

---

### Conditional Expressions

group name: ConditionalExpressions

#### Boolean - And into Or

name: AndToOr

description: Changes the conditional operator `&&` to `||` on `Boolean` type.

#### Boolean - Or into And

name: OrToAnd

description: Changes the conditional operator `||` to `&&` on `Boolean` type.

#### Boolean - Remove negation

name: RemoveUnaryNot

description: Removes the `!` operator on `Boolean` type.

example:

```diff
- if (!value) 10 else 20
+ if (value) 10 else 20
```

---

### Literal Strings

group name: LiteralStrings

#### String - Change empty string

name: EmptyToMutated

description: Changes empty strings `""` into `"mutated!"`.

example:

```diff
- val str = ""
+ val str = "mutated!"
```

#### String - Change empty interpolated string

name: EmptyInterToMutated

description: Changes empty interpolated strings `s""` into `"mutated!"`. It works on `s`, `f` and `raw` interpolators.

example:

```diff
- val str = s""
+ val str = "mutated!"
```

#### String - Change non-empty string

name: NonEmptyToMutated

description: Changes non-empty strings into two mutations. One with empty `""` and another with `"mutated!"`.

example mutation 1:

```diff
- val str = "foobar"
+ val str = ""
```

example mutation 2:

```diff
- val str = "foobar"
+ val str = "mutated!"
```

#### String - Change non-empty interpolated string

name: NonEmptyInterToMutated

description: Changes non-empty interpolated strings into two mutations. One with empty `""` and another with `"mutated!"`. It works on `s`, `f` and `raw` interpolators.

example mutation 1:

```diff
- val str = s"forbar: $foobar"
+ val str = ""
```

example mutation 2:

```diff
- val str = s"forbar: $foobar"
+ val str = "mutated!"
```

---

### Scala Options

group name: ScalaOptions

#### GetOrElse

name: GetOrElse

description: Changes the scala.Option `getOrElse` function into two mutations. 
One with the `getOrElse` function replaced by `get` and another with just the default value.

example mutation 1: (is the 'get' part tested?)

```diff
  // option: Option[Int]
- val value = option.getOrElse(10)
+ val value = option.get
```

example mutation 2: (is the 'orElse' part tested?)

```diff
  // option: Option[Int]
- val value = option.getOrElse(10)
+ val value = 10
```

#### Exists

name: Exists

description: Changes the scala.Option `exists` into `forall`.

#### Forall

name: Forall

description: Changes the scala.Option `forall` into `exists`.

#### IsEmpty

name: IsEmpty

description: Changes the scala.Option `isEmpty` into `nonEmpty`.

#### NonEmpty

name: NonEmpty

description: Changes the scala.Option `nonEmpty` and `isDefined` into `isEmpty`.

#### Fold

name: Fold

description: Changes the scala.Option `fold` function call into the default value.

example:

```diff
  // option: Option[Int]
- val value = option.fold(10)(v => v * 2)
+ val value = 10
```

#### OrElse

name: OrElse

description: Changes the scala.Option `orElse` function into two mutations.
One with just the call expression (left side) and another with the orElse argument (right side).

example mutation 1:

```diff
  // option: Option[Int]
- val value = option.map(x => x * 3).orElse(Some(0))
+ val value = option.map(x => x * 3)
```

example mutation 2:

```diff
  // option: Option[Int]
- val value = option.map(x => x * 3).orElse(Some(0))
+ val value = Some(0)
```

#### OrNull

name: OrNull

description: Changes the scala.Option `orNull` function into the value null.

example:

```diff
  // option: Option[Int]
- val value = option.orNull
+ val value = null
```

#### Filter

name: Filter

description: Changes the scala.Option `filter` function into two mutations. One that calls `filterNot` and another that doesn't call anything (does not filter)

example mutation 1:

```diff
  // option: Option[Int]
- val value = option.filter(v => v > 2)
+ val value = option.filterNot(v => v > 2)
```

example mutation 2:

```diff
  // option: Option[Int]
- val value = option.filter(v => v > 2)
+ val value = option
```

#### FilterNot

name: FilterNot

description: Changes the scala.Option `filterNot` function into two mutations. One that calls `filter` and another that doesn't call anything (does not filter)

example mutation 1:

```diff
  // option: Option[Int]
- val value = option.filterNot(v => v > 2)
+ val value = option.filter(v => v > 2)
```

example mutation 2:

```diff
  // option: Option[Int]
- val value = option.filterNot(v => v > 2)
+ val value = option
```

#### Contains

name: Contains

description: Changes the scala.Option `contains` function into two mutations. One with `true` and another with `false`.

example mutation 1:

```diff
  // option: Option[Int]
- val value = option.contains(10)
+ val value = true
```

example mutation 2:

```diff
  // option: Option[Int]
- val value = option.contains(10)
+ val value = false
```

---

### Scala Try

classpath: scala.util.Try

group name: ScalaTry

#### GetOrElse

name: GetOrElse

description: Changes the scala.util.Try `getOrElse` function into two mutations. 
One with the `getOrElse` function replaced by `get` and another with just the default value.

example mutation 1: (is the 'get' part tested?)

```diff
  // tryValue: Try[Int]
- val value = tryValue.getOrElse(10)
+ val value = tryValue.get
```

example mutation 2: (is the 'orElse' part tested?)

```diff
  // tryValue: Try[Int]
- val value = tryValue.getOrElse(10)
+ val value = 10
```

#### OrElse

name: OrElse

description: Changes the scala.util.Try `orElse` function into two mutations.
One with just the call expression (left side) and another with the orElse argument (right side).

example mutation 1:

```diff
  // tryValue: Try[Int]
- val value = tryValue.orElse(Try(0))
+ val value = tryValue
```

example mutation 2:

```diff
  // tryValue: Try[Int]
- val value = tryValue.orElse(Try(0))
+ val value = Try(0)
```

---

### Scala Collections

group name: Collections

#### ListApply

name: ListApply

description: Changes the scala.collection.immutable.List `apply` arguments.
Creating mutations where one of the arguments is missing.

example mutation 1:

```diff
- val list = List("a", "b", "c")
+ val list = List("b", "c")
```

example mutation 2:

```diff
- val list = List("a", "b", "c")
+ val list = List("a", "c")
```

example mutation 3:

```diff
- val list = List("a", "b", "c")
+ val list = List("a", "b")
```

#### SeqApply

name: SeqApply

description: Changes the Seq `apply` arguments.
(it works for `scala.Seq`, `scala.collection.mutable.Seq` and `scala.collection.immutable.Seq`)
Creating mutations where one of the arguments is missing.

example mutation 1:

```diff
- val seq = Seq("a", "b", "c")
+ val seq = Seq("b", "c")
```

example mutation 2:

```diff
- val seq = Seq("a", "b", "c")
+ val seq = Seq("a", "c")
```

example mutation 3:

```diff
- val seq = Seq("a", "b", "c")
+ val seq = Seq("a", "b")
```

#### SetApply

name: SetApply

description: Changes the Set `apply` arguments.
(it works for `scala/Predef.Set`, `scala.collection.mutable.Set` and `scala.collection.immutable.Set`)
Creating mutations where one of the arguments is missing.

example mutation 1:

```diff
- val set = Set("a", "b", "c")
+ val set = Set("b", "c")
```

example mutation 2:

```diff
- val set = Set("a", "b", "c")
+ val set = Set("a", "c")
```

example mutation 3:

```diff
- val set = Set("a", "b", "c")
+ val set = Set("a", "b")
```

#### Reverse

name: Reverse

description: Removes the call to `reverse` on `List`/`SeqLike`/`IndexedSeqOptimized`.

example mutation 1:

```diff
- val list = List("a", "b").reverse
+ val list = List("a", "b")
```

example mutation 2:

```diff
- val seq = Seq("a", "b").reverse
+ val seq = Seq("a", "b")
```

#### Drop

name: Drop

description: Removes the call to `drop` on `List`/`SeqLike`/`IndexedSeqOptimized`.

example mutation 1:

```diff
- val list = List("a", "b", "c").drop(2)
+ val list = List("a", "b", "c")
```

example mutation 2:

```diff
- val seq = Seq("a", "b", "c").drop(2)
+ val seq = Seq("a", "b", "c")
```

#### Take

name: Take

description: Removes the call to `take` on `List`/`SeqLike`/`IndexedSeqOptimized`.

example mutation 1:

```diff
- val list = List("a", "b", "c").take(2)
+ val list = List("a", "b", "c")
```

example mutation 2:

```diff
- val seq = Seq("a", "b", "c").take(2)
+ val seq = Seq("a", "b", "c")
```

#### ReduceOption

name: ReduceOption

description: Replaces the call to `reduceOption` on `List`/`SeqLike`/`IndexedSeqOptimized` with `None`.

example mutation 1:

```diff
- val list = List(1, 2, 3).reduceOption((a, b) => a + b)
+ val list = None
```

example mutation 2:

```diff
- val seq = Seq[Int]().reduceOption((a, b) => a + b)
+ val seq = None
```

#### Prepend

name: Prepend

description: Removes the call to `prepend` on `List`/`SeqOps`/`ArrayOps` and also `::` on `List`.

example mutation 1:

```diff
- val list = "a" :: List("b", "c")
+ val list = List("b", "c")
```

example mutation 2:

```diff
- val list = List("b", "c").prepended("a")
+ val list = List("b", "c")
```

example mutation 3:

```diff
- val seq = Seq("b", "c").prepended("a")
+ val seq = Seq("b", "c")
```

---

### Partial Functions

group name: PartialFunctions

#### RemoveOneCase

name: RemoveOneCase

description: Changes partial function applications, 
creating mutations where one of the cases is unreachable.

example mutation 1:

```diff
  // strList: List[String]
  strList.collect {
-   case "foo" => "foobar"
+   case "foo" if false => "foobar"
    case "bar" => "barfoo"
  }
```

example mutation 2:

```diff
  // strList: List[String]
  strList.collect {
    case "foo" => "foobar"
-   case "bar" => "barfoo"
+   case "bar" if false => "barfoo"
  }
```

#### RemoveOneAlternative

name: RemoveOneAlternative

description: Changes partial function applications, 
creating mutations where one of the pattern alternatives is missing.

example mutation 1:

```diff
  // strList: List[String]
  strList.collect {
-   case "foo" | "bar => "foobar"
+   case "foo" => "foobar"
  }
```

example mutation 2:

```diff
  // strList: List[String]
  strList.collect {
-   case "foo" | "bar => "foobar"
+   case "bar" => "foobar"
  }
```

---

### Scala Strings

group name: ScalaStrings

#### String - Concat

name: Concat

description: Changes the string concat operator `+` and `concat` and both left and right expressions into two mutations. One with empty `""` and another with `"mutated!"`.

example mutation 1:

```diff
- val str = "foo" + "bar"
+ val str = ""
```

example mutation 2:

```diff
- val str = "foo" + "bar"
+ val str = "mutated!"
```

example mutation 3:

```diff
- val str = "foo".concat("bar")
+ val str = ""
```

example mutation 4:

```diff
- val str = "foo".concat("bar")
+ val str = "mutated!"
```

#### Trim

name: Trim

description: Removes the call to `trim` on strings. 

example:

```diff
- val value = " foo ".trim
+ val value = " foo "
```

#### ToUpperCase

name: ToUpperCase

description: Removes the call to `toUpperCase` on strings. 

example:

```diff
- val value = " foo ".toUpperCase
+ val value = " foo "
```

#### ToLowerCase

name: ToLowerCase

description: Removes the call to `toLowerCase` on strings. 

example:

```diff
- val value = " foo ".toLowerCase
+ val value = " foo "
```

#### Capitalize

name: Capitalize

description: Removes the call to `capitalize` on strings.

example:

```diff
- val value = "foo".capitalize
+ val value = "foo"
```

#### StripPrefix

name: StripPrefix

description: Removes the call to `stripPrefix` on strings.

example:

```diff
- val value = "Foo".stripPrefix("F")
+ val value = "Foo"
```

#### StripSuffix

name: StripSuffix

description: Removes the call to `stripSuffix` on strings.

example:

```diff
- val value = "Foo".stripSuffix("oo")
+ val value = "Foo"
```

#### Drop

name: Drop

description: Removes the call to `drop` on strings.

example:

```diff
- val value = "Foo123".drop(3)
+ val value = "Foo123"
```

#### Take

name: Take

description: Removes the call to `take` on strings.

example:

```diff
- val value = "Foo123".take(4)
+ val value = "Foo123"
```

#### DropWhile

name: DropWhile

description: Removes the call to `dropWhile` on strings.

example:

```diff
- val value = "Foo123".dropWhile(char => char.isLetter)
+ val value = "Foo123"
```

#### TakeWhile

name: TakeWhile

description: Removes the call to `takeWhile` on strings.

example:

```diff
- val value = "Foo123".takeWhile(char => char.isLetter)
+ val value = "Foo123"
```

#### Map

name: Map

description: Removes the call to `map(Char => Char)` on strings.

example:

```diff
- val value = "Foo123".map(char => char.toUpper)
+ val value = "Foo123"
```

#### FlatMap

name: FlatMap

description: Removes the call to `flatMap(Char => String)` on strings.

example:

```diff
- val value = "Foo123".flatMap(char => s"$char$char")
+ val value = "Foo123"
```

#### Reverse

name: Reverse

description: Removes the call to `reverse` on Strings.

example mutation:

```diff
- val str = "tacocat".reverse
+ val str = "tacocat"
```

---

### Control Flow

group name: ControlFlow

#### If control flow

name: If

description: Changes the standard if control flow into two mutants.
One that always go to the then part and another that always go to the else part.
If the else part doesn't exist then it's replaced with the mutant `()`  

example mutation 1:

```diff
- if (condition) somethingThen() else somethingElse()
+ somethingThen()
```

example mutation 2:

```diff
- if (condition) somethingThen() else somethingElse()
+ somethingElse()
```

example mutation 3:

```diff
- if (condition) somethingThen()
+ somethingThen()
```

example mutation 4:

```diff
- if (condition) somethingThen()
+ ()
```

---

### ZIO

group name: ZIO

#### When

name: When

description: Changes the `when` call into three mutants.

example mutation 1:

```diff
- ZIO.succeed("abc").when(true)
+ ZIO.succeed("abc").unless(true)
```

example mutation 2:

```diff
- ZIO.succeed("abc").when(true)
+ ZIO.succeed("abc").asSome
```

example mutation 3:

```diff
- ZIO.succeed("abc").when(true)
+ ZIO.succeed("abc").as(None)
```

#### Unless

name: Unless

description: Changes the `unless` call into three mutants.

example mutation 1:

```diff
- ZIO.succeed("abc").unless(true)
+ ZIO.succeed("abc").when(true)
```

example mutation 2:

```diff
- ZIO.succeed("abc").unless(true)
+ ZIO.succeed("abc").asSome
```

example mutation 3:

```diff
- ZIO.succeed("abc").unless(true)
+ ZIO.succeed("abc").as(None)
```

---