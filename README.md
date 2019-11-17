# Blinky

[![Build Status](https://travis-ci.com/RCMartins/blinky.svg?branch=master)](https://travis-ci.com/RCMartins/blinky)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9bc5c989d1464a6ca94da021ee43d8f6)](https://www.codacy.com/manual/RCMartins/blinky?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RCMartins/blinky&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9bc5c989d1464a6ca94da021ee43d8f6)](https://www.codacy.com/manual/RCMartins/blinky?utm_source=github.com&utm_medium=referral&utm_content=RCMartins/blinky&utm_campaign=Badge_Coverage)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.rcmartins/blinky_2.12.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.rcmartins%22%20AND%20a:%22blinky_2.12%22)

Mutation testing is a type of software testing where we mutate (change) certain expressions in the source code 
and check if the test cases are able to find the errors.
It is a type of White Box Testing which is mainly used for Unit Testing.

**Blinky** has 3 main steps:
* Copy the git project to a temporary folder (where the source code can be safely modified)
* Run the scalafix tool with the **Blinky** rule (on the copy project)
* Run the tests on mutated code (usually with only 1 mutation active each time)

We use **Blinky** to test itself and to improve the test code quality.

Similar projects:
* [scalamu](https://github.com/sugakandrey/scalamu)
* [stryker4s](https://github.com/stryker-mutator/stryker4s)

The main difference in this project is that the mutations are semantic instead of just syntactic.
Meaning that when using a rule like `ScalaOptions.filter` we only mutate calls to
the method `filter` of objects of type `scala.Option`.
In order to have this semantic information about the types **Blinky** 
needs the [semanticdb](https://scalameta.org/docs/semanticdb/guide.html)
data of all files that we want to mutate.

## Generating semanticdb files for your sbt project
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

## How to run Blinky

First, install the [Coursier](https://get-coursier.io/docs/cli-overview) command-line interface.

Next, write `.blinky.conf` config file with the path of the project you want to run **Blinky**, e.g
```hocon
projectPath = "/project"
filesToMutate = "src"
testCommand = "tests"
options = {
  maxRunningTime = 10 minutes
}
```

Next, launch **Blinky** (it will use .blinky.conf file by default)

```
coursier launch com.github.rcmartins:blinky-cli_2.12:0.2.0 --main blinky.cli.Cli
```

Blinky cli will compile the project, generating the necessary semanticdb files
before applying the mutations to the code.

## Configuration

Blinky reads configuration from a file `.blinky.conf`, usually in the root directly of your project.
Configuration is written using [HOCON](https://github.com/lightbend/config) syntax.

### projectPath (required)
The path to the project 

### filesToMutate (required)
Files or directories (recursively visited) to apply mutations (used directly in scalafix --files= param)

### testCommand (required)
Command used by **Blinky** to test the code in each run.

If using Bloop, **Blinky** will run: `bloop test <testCommand>`

If using SBT, **Blinky** will run: `sbt <testCommand>`

### options (optional)
Advanced options to configure how **Blinky** will run tests.

#### verbose
Boolean flag to show additional information useful for debugging.

Default: `false`

#### dryRun
Will not run the tests, useful to check if everything is working without starting
to actually run the long and compute intensive part of running the tests.

Default: `false`

#### compileCommand
Command used by Blinky to do the first compile before starting to runs the tests.
This is useful to calculate the time that the first test takes without counting compiling.

Default: `compile`

#### maxRunningTime
Maximum time to run tests.

Default: `60 minutes`

#### mutationMinimum
Minimum mutation score to fail (only useful if failOnMinimum is `true`).
Value must be between 0 and 100.

The mutation score is `mutations killed / total mutations tested`.
E.g. If we test `200` mutations and `90` are killed by the tests the mutation score will be `45.0`.

Default: `25.0`

#### failOnMinimum
Exits with non-zero code if the mutation score is below `mutationMinimum` value.

Default: `false`

### conf (optional)
Configuration for applying the mutations.

#### enabledMutators (optional)
List of mutators that will be enabled. Most mutators are in groups, the whole group can be enabled,
or just single mutators.

example:
```hocon
conf {
  enabledMutators = [ArithmeticOperators, LiteralStrings.EmptyToMutated]
}
``` 
This configuration will enable all mutators from `ArithmeticOperators` group and also the mutator
`EmptyToMutated` from the group `LiteralStrings`.

Default value: `[all]` 

#### disabledMutators (optional)
List of mutators that will be disabled, can be used together with `enabledMutators`.

example:
```hocon
conf {
  disabledMutators = [
    LiteralBooleans
    ArithmeticOperators.IntMulToDiv
    ScalaOptions.OrElse
  ]
}
```
This configuration will allow all mutators with the exception 
of `LiteralBooleans`, `IntMulToDiv` and `OrElse`.
 
Default value: `[]`

Example of a more complete `.blinky.conf` file:
```hocon
projectPath = "."
filesToMutate = "blinky-core/src"
testCommand = "tests"
options = {
  verbose = false
  dryRun = false
  compileCommand = compile

  maxRunningTime = 40 minutes

  failOnMinimum = true
  mutationMinimum = 50
}
conf = {
  enabledMutators = [
    ArithmeticOperators
    LiteralStrings.EmptyToMutated
    ScalaOptions
  ]
  disabledMutators = [
    { ScalaOptions = [GetOrElse, Contains] }
  ]
}
```

## Mutators

Mutators are the transformations to the code that we want to apply.
Because of several factors like time to run or importance we may want to enable/disable some
of the available mutators.

## Available Mutators

### Literal Booleans

name: LiteralBooleans

description: change true into false and false into true.  

example:

```diff
- val bool = true
+ val bool = false
```

### Arithmetic Operators

group name: ArithmeticOperators

#### Int - Plus into Minus

name: IntPlusToMinus

description: Changes the arithmetic operator `+` into `-` when operating on `int` types.

example:

```diff
- val value = list.size + 5
+ val value = list.size - 5
```

(Note that it only applies to `int` type)

#### Int - Minus into Plus

name: IntMinusToPlus

description: Changes the arithmetic operator `-` into `+` when operating on `int` types.

#### Int - Multiply into Divide

name: IntMulToDiv

description: Changes the arithmetic operator `*` into `/` when operating on `int` types.

#### Int - Divide into Multiply

name: IntDivToMul

description: Changes the arithmetic operator `/` into `*` when operating on `int` types.

### Conditional Expressions

group name: ConditionalExpressions

#### Boolean - And into Or

name: AndToOr

description: Changes the conditional operator `&&` to `||` on `boolean` types.

#### Boolean - Or into And

name: OrToAnd

description: Changes the conditional operator `||` to `&&` on `boolean` types.

#### Boolean - Remove negation

name: RemoveUnaryNot

description: Removes the `!` operator on `boolean` types.

example:

```diff
- if (!value) 10 else 20
+ if (value) 10 else 20
```

### Literal Strings

group name: LiteralStrings

#### String - Change empty string

name: EmptyToMutated

description: Changes empty strings `""` into `"mutated!"`.

example:

```diff
- val name = ""
+ val name = "mutated!"
```

#### String - Change non-empty string

name: NonEmptyToMutated

description: Changes non-empty strings into two mutations. One with empty `""` and another with `"mutated!"`.

example mutation 1:

```diff
- val name = "foobar"
+ val name = ""
```

example mutation 2:

```diff
- val name = "foobar"
+ val name = "mutated!"
```

#### String - Concat into mutated

name: ConcatToMutated

description: Changes the string concat operator `+` and both left and right expressions into two mutations. One with empty `""` and another with `"mutated!"`.

example mutation 1:

```diff
- val name = "foo" + "bar"
+ val name = ""
```

example mutation 2:

```diff
- val name = "foo" + "bar"
+ val name = "mutated!"
```

### Scala Options

group name: ScalaOptions

#### GetOrElse

name: GetOrElse

description: Changes the scala.Option `getOrElse` function call into the default value.

example:

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

description: Changes the scala.Option `orElse` function into two mutations. One with just the call expression (left side) and another with the orElse argument (right side).

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
