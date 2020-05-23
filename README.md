# Blinky

[![Build Status][badge-travis]][link-travis]
[![Codacy Badge][badge-codacy]][link-codacy]
[![Codacy Badge][badge-coverage]][link-coverage]
[![Maven Central][badge-release]][link-release]
[![Maven Central][badge-snapshot]][link-snapshot]
[![Scala Steward badge][badge-scalasteward]][link-scalasteward]

Mutation testing is a type of software testing where we mutate (change) certain expressions in the source code 
and check if the test cases are able to find the errors.
It is a type of White Box Testing which is mainly used for Unit Testing.

_Blinky_ has 3 main steps:
* Copy the git project to a temporary folder (where the source code can be safely modified)
* Run the scalafix tool with the _Blinky_ rule (on the copy project)
* Run the project tests on the mutated code (usually with only 1 mutation active each time)

We use _Blinky_ to test itself and to improve the test code quality.

Similar projects:
* [scalamu](https://github.com/sugakandrey/scalamu)
* [stryker4s](https://github.com/stryker-mutator/stryker4s)

The main difference is that in _Blinky_ the mutations are semantic instead of just syntactic.
Meaning when using a rule like `ScalaOptions.filter` we only mutate calls to
the method `filter` of objects of type `scala.Option`.
In order to have this semantic information about the types _Blinky_ 
needs the [semanticdb](https://scalameta.org/docs/semanticdb/guide.html)
data of all files that we want to mutate.

## Generating semanticdb files for your sbt project

_Blinky_ will automatically add semanticdb in your sbt project.
At the moment this is a limitation that only allows the following scalaVersions:
  * 2.13.2
  * 2.13.1
  * 2.13.0
  * 2.12.11 (fully tested)
  * 2.12.10
  * 2.12.9
  * 2.12.8
  * 2.11.12

SBT has to be >= **1.3.4**

## How to run Blinky

First, install the [Coursier](https://get-coursier.io/docs/cli-overview) command-line interface.

Next, write `.blinky.conf` config file with the path of the project you want to run _Blinky_, e.g
```hocon
projectPath = "."
projectName = "blinky"
filesToMutate = "src/main/scala"
options = {
  maxRunningTime = 10 minutes
}
```

Next, launch _Blinky_ (it will use .blinky.conf file by default)

Last stable release: [![Maven Central][badge-release]][link-release]

Last unstable release: [![Maven Central][badge-snapshot]][link-snapshot]
```
coursier launch com.github.rcmartins:blinky-cli_2.12:0.2.1 --main blinky.cli.Cli
```

Blinky cli will compile the project and generate the necessary semanticdb files
before applying the mutations to the code.

You can also pass parameters directly to override the .blinky.conf file: 
```
coursier launch com.github.rcmartins:blinky-cli_2.12:0.2.1 --main blinky.cli.Cli -- --onlyMutateDiff=true
```

## Configuration

Blinky reads configuration from a file `.blinky.conf`, usually in the root directly of your project.
Configuration uses [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) syntax.

### projectPath (optional)
The path to the project. 

Default: `.` (current directory)

### filesToMutate (optional)
Files or directories (recursively visited) to apply mutations (used directly in scalafix --files= param).

Default: `src/main/scala`

### filesToExclude (optional)
Unix-style glob for files to exclude from being mutated by `filesToMutate` param.
The glob syntax is defined by [nio.FileSystem.getPathMatcher](https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-).
(Used directly in scalafix --exclude= param).

Default: `""`

### options (optional)
Advanced options to configure how _Blinky_ will run tests.

#### verbose
A boolean flag to show additional information useful for debugging.

Default: `false`

#### dryRun
Will not run the mutation tests, useful to check if everything is working without starting
to actually run the long and compute-intensive part of running the tests.

Default: `false`

#### compileCommand
Command used by _Blinky_ to do the first compile before starting to run the tests.
This is useful to calculate the time the first test takes without counting compiling.

If using Bloop, _Blinky_ will run: `bloop compile <compileCommand>`

**(Not implemented)**
If using SBT, _Blinky_ will run: `sbt <compileCommand>`

Default: `""`

#### testCommand
Command used by _Blinky_ to test the code in each run.

If using Bloop, _Blinky_ will run: `bloop test <testCommand>`

**(Not implemented)**
If using SBT, _Blinky_ will run: `sbt <testCommand>`

Default: `""`

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

#### onlyMutateDiff
If set, only mutates added and edited files in git diff against the master branch.

Default: false

### mutators (optional)
Configuration for applying the mutations.

#### enabled (optional)
List of mutators that will be enabled. Most mutators are in groups, the whole group can be enabled,
or just single mutators.

example:
```hocon
mutators {
  enabled = [ArithmeticOperators, LiteralStrings.EmptyToMutated]
}
``` 
This configuration will enable all mutators from `ArithmeticOperators` group and the mutator
`EmptyToMutated` from the group `LiteralStrings`.

Default value: `[all]` 

#### disabled (optional)
List of mutators that will be disabled, can be used together with `enabledMutators`.

example:
```hocon
mutators {
  disabled = [
    LiteralBooleans
    ArithmeticOperators.IntMulToDiv
    ScalaOptions.OrElse
  ]
}
```
This configuration will allow all mutators except `LiteralBooleans`, `IntMulToDiv` and `OrElse`.
 
Default value: `[]`

---

Example of a more complete `.blinky.conf` file:
```hocon
projectPath = "."
filesToMutate = "blinky-core/src"
filesToExclude = "**/internal/*scala"
options = {
  verbose = false
  dryRun = false
  compileCommand = "tests"
  testCommand = "tests"
  maxRunningTime = 40 minutes
  failOnMinimum = true
  mutationMinimum = 50
  onlyMutateDiff = true
}
mutators = {
  enabled = [
    ArithmeticOperators
    LiteralStrings.EmptyToMutated
    ScalaOptions
  ]
  disabled = [
    { ScalaOptions = [GetOrElse, Contains] }
  ]
}
```

## Mutators

Mutators are the transformations to the code that we want to apply.
Because of several factors like time to run or importance we may want to enable/disable some available mutators.

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

description: Changes the arithmetic operator `+` into `-` when operating on `Int` type.

example:

```diff
- val value = list.size + 5
+ val value = list.size - 5
```

(Note that it only applies to `Int` type)

#### Int - Minus into Plus

name: IntMinusToPlus

description: Changes the arithmetic operator `-` into `+` when operating on `Int` type.

#### Int - Multiply into Divide

name: IntMulToDiv

description: Changes the arithmetic operator `*` into `/` when operating on `Int` type.

#### Int - Divide into Multiply

name: IntDivToMul

description: Changes the arithmetic operator `/` into `*` when operating on `Int` type.

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

[badge-travis]: https://travis-ci.com/rcmartins/blinky.svg?branch=master "build"
[badge-codacy]: https://api.codacy.com/project/badge/grade/9bc5c989d1464a6ca94da021ee43d8f6 "codacy"
[badge-coverage]: https://api.codacy.com/project/badge/coverage/9bc5c989d1464a6ca94da021ee43d8f6 "coverage"
[badge-release]: https://img.shields.io/maven-central/v/com.github.rcmartins/blinky_2.12.svg?label=maven%20central "release"
[badge-snapshot]: https://img.shields.io/nexus/s/com.github.rcmartins/blinky-cli_2.12?server=https%3a%2f%2foss.sonatype.org "snapshot"
[badge-scalasteward]: https://img.shields.io/badge/scala_steward-helping-blue.svg?style=flat&logo=data:image/png;base64,ivborw0kggoaaaansuheugaaaa4aaaaqcamaaaarsr4iaaaavfbmveuaaachjojloy5nwlrkzcyrkjgfjibp293yycula3pyy2lsqql4f3pcuftgsjnodyrmcxuspd/nttbjrs+2jomhgnznc223cgvzs0hasd0xljbasjelhir+aaaaaxrstlmaqobyzgaaahljrefucndnyosowyaihwhaqs1vt7a77/3fcxxdmv0xwmckutar1nkm4ggbyecg/wwmlgldaa3ol50xi6fk5ffz3e2e3qfzdcccn2ytbewzt+drc6u6rlqv7uk0ldkqqr5rk2ucrxok0vmqkgfc94nojyqjouf9h/wcc9geceyfonoaaaaasuvork5cyii= "scala steward"

[link-travis]: https://travis-ci.com/rcmartins/blinky "build"
[link-codacy]: https://www.codacy.com/manual/rcmartins/blinky?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rcmartins/blinky&amp;utm_campaign=badge_grade "codacy"
[link-coverage]: https://www.codacy.com/manual/rcmartins/blinky?utm_source=github.com&utm_medium=referral&utm_content=rcmartins/blinky&utm_campaign=badge_coverage "coverage"
[link-release]: https://search.maven.org/search?q=g:%22com.github.rcmartins%22%20and%20a:%22blinky_2.12%22 "release"
[link-snapshot]: https://oss.sonatype.org/content/repositories/snapshots/com/github/rcmartins/blinky-cli_2.12 "snapshot"
[link-scalasteward]: https://scala-steward.org "scala steward"