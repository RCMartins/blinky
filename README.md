[![Build Status](https://travis-ci.com/RCMartins/blinky.svg?branch=master)](https://travis-ci.com/RCMartins/blinky)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9bc5c989d1464a6ca94da021ee43d8f6)](https://www.codacy.com/manual/RCMartins/blinky?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RCMartins/blinky&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9bc5c989d1464a6ca94da021ee43d8f6)](https://www.codacy.com/manual/RCMartins/blinky?utm_source=github.com&utm_medium=referral&utm_content=RCMartins/blinky&utm_campaign=Badge_Coverage)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.rcmartins/blinky_2.12.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.rcmartins%22%20AND%20a:%22blinky_2.12%22)



Mutation testing is a type of software testing where we mutate (change) certain expressions in the source code 
and check if the test cases are able to find the errors.
It is a type of White Box Testing which is mainly used for Unit Testing.

This tool has 3 main steps:
* Copy the git project to a temporary folder (where the source code can be safelly modified)
* Run the scalafix tool with the *Blinky* rule (on the copy project)
* Run the tests on mutated code (usually with only 1 mutation active each time)

We use mutation testing to test the tool itself and to improve the code quality.

Similar projects:
* https://github.com/sugakandrey/scalamu
* https://github.com/stryker-mutator/stryker4s

The main difference in this project is that the mutations are semantic.
Meaning that when using a rule like *ScalaOptions.filter* we only change calls to
the method *filter* of objects of type *scala.Option*.
In order to have this information about the types blinky 
it needs the [semanticdb](https://scalameta.org/docs/semanticdb/guide.html)
data for all the files that we want to add mutations to.

## How to generate semanticdb files for your sbt project:
Before sbt 1.3.0:
```scala
libraryDependencies += "org.scalameta" % "semanticdb-scalac" % "4.2.3" cross CrossVersion.full
scalacOptions += "-Yrangepos"
```
After sbt 1.3.0:
```scala
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := "4.1.9"
ThisBuild / semanticdbIncludeInJar := false
```

Blinky-cli will compile the project, so  

# Mutators

Mutators are the transformations to the code that we want to apply.
Because of several factors like time to run or importance we may want to enable/disable some
of the available mutators.

# Available Mutators

## Literal Booleans
#### name: LiteralBooleans
#### description: change true into false and false into true.  

#### example:

```diff
- val bool = true
+ val bool = false
```

## Arithmetic Operators

### group name: ArithmeticOperators

### Int - Plus into Minus
#### name: IntPlusToMinus
#### description: Changes the arithmetic operator `+` into `-` when operating on `int` types.

#### example:

```diff
- val value = list.size + 5
+ val value = list.size - 5
```

(Note that it only applies to `int` types)

### Int - Minus into Plus
#### name: IntMinusToPlus
#### description: Changes the arithmetic operator `-` into `+` when operating on `int` types.

### Int - Multiply into Divide
#### name: IntMulToDiv
#### description: Changes the arithmetic operator `*` into `/` when operating on `int` types.

### Int - Divide into Multiply
#### name: IntDivToMul
#### description: Changes the arithmetic operator `/` into `*` when operating on `int` types.

## Conditional Expressions

### group name: ConditionalExpressions

### Boolean - And into Or
#### name: AndToOr
#### description: Changes the conditional operator `&&` to `||` on `boolean` types.

### Boolean - Or into And
#### name: OrToAnd
#### description: Changes the conditional operator `||` to `&&` on `boolean` types.

### Boolean - Remove negation
#### name: RemoveUnaryNot
#### description: Removes the `!` operator on `boolean` types.

#### example:

```diff
- if (!value) 10 else 20
+ if (value) 10 else 20
```

## Literal Strings

### group name: LiteralStrings

### String - Change empty string
#### name: EmptyToMutated
#### description: Changes empty strings `""` into `"mutated!"`.

#### example:

```diff
- val name = ""
+ val name = "mutated!"
```

### String - Change non-empty string
#### name: NonEmptyToMutated
#### description: Changes non-empty strings into two mutations. One with empty `""` and another with `"mutated!"`.

#### example mutation 1:

```diff
- val name = "foobar"
+ val name = ""
```

#### example mutation 2:

```diff
- val name = "foobar"
+ val name = "mutated!"
```

### String - Concat into mutated
#### name: ConcatToMutated
#### description: Changes the string concat operator `+` and both left and right expressions into two mutations. One with empty `""` and another with `"mutated!"`.

#### example mutation 1:

```diff
- val name = "foo" + "bar"
+ val name = ""
```

#### example mutation 2:

```diff
- val name = "foo" + "bar"
+ val name = "mutated!"
```

## Scala Options

### group name: ScalaOptions

### GetOrElse

#### name: GetOrElse
#### description: Changes the scala.Option `getOrElse` function call into the default value.

#### example:

```diff
  // option: Option[Int]
- val value = option.getOrElse(10)
+ val value = 10
```

### Exists

#### name: Exists
#### description: Changes the scala.Option `exists` into `forall`.

### Forall

#### name: Forall
#### description: Changes the scala.Option `forall` into `exists`.

### IsEmpty

#### name: IsEmpty
#### description: Changes the scala.Option `isEmpty` into `nonEmpty`.

### NonEmpty

#### name: NonEmpty
#### description: Changes the scala.Option `nonEmpty` and `isDefined` into `isEmpty`.

### Fold

#### name: Fold
#### description: Changes the scala.Option `fold` function call into the default value.

#### example:

```diff
  // option: Option[Int]
- val value = option.fold(10)(v => v * 2)
+ val value = 10
```

### OrElse

#### name: OrElse
#### description: Changes the scala.Option `orElse` function into two mutations. One with just the call expression (left side) and another with the orElse argument (right side).

#### example mutation 1:

```diff
  // option: Option[Int]
- val value = option.map(x => x * 3).orElse(Some(0))
+ val value = option.map(x => x * 3)
```

#### example mutation 2:

```diff
  // option: Option[Int]
- val value = option.map(x => x * 3).orElse(Some(0))
+ val value = Some(0)
```

### OrNull

#### name: OrNull
#### description: Changes the scala.Option `orNull` function into the value null.

#### example:

```diff
  // option: Option[Int]
- val value = option.orNull
+ val value = null
```

### Filter

#### name: Filter
#### description: Changes the scala.Option `filter` function into two mutations. One that calls `filterNot` and another that doesn't call anything (does not filter)

#### example mutation 1:

```diff
  // option: Option[Int]
- val value = option.filter(v => v > 2)
+ val value = option.filterNot(v => v > 2)
```

#### example mutation 2:

```diff
  // option: Option[Int]
- val value = option.filter(v => v > 2)
+ val value = option
```

### FilterNot

#### name: FilterNot
#### description: Changes the scala.Option `filterNot` function into two mutations. One that calls `filter` and another that doesn't call anything (does not filter)


#### example mutation 1:

```diff
  // option: Option[Int]
- val value = option.filterNot(v => v > 2)
+ val value = option.filter(v => v > 2)
```

#### example mutation 2:

```diff
  // option: Option[Int]
- val value = option.filterNot(v => v > 2)
+ val value = option
```

### Contains

#### name: Contains
#### description: Changes the scala.Option `contains` function into two mutations. One with `true` and another with `false`.

#### example mutation 1:

```diff
  // option: Option[Int]
- val value = option.contains(10)
+ val value = true
```

#### example mutation 2:

```diff
  // option: Option[Int]
- val value = option.contains(10)
+ val value = false
```
