---
id: configuration
title: Configuration
sidebar_label: Configuration
---

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
to actually run the long and compute-intensive part of running the mutated tests.

Default: `false`

#### copyGitFolder
If set, _Blinky_ will copy the original `.git` folder to the temporary directory used
to run the tests.

Default: `false`

#### testRunner
The test runner to use. Currently supported runners are: `bloop` and `sbt`.

Default: `bloop`

#### compileCommand
Command used by _Blinky_ to do the first compile before starting to run the tests.
This is useful to calculate the time the first test takes without counting compiling.

If using Bloop, _Blinky_ will run: `bloop compile <compileCommand>`

If using SBT, _Blinky_ will run: `sbt <compileCommand>`

Default: `""`

#### testCommand
Command used by _Blinky_ to test the code in each run.

If using Bloop, _Blinky_ will run: `bloop test <testCommand>`

If using SBT, _Blinky_ will run: `sbt <testCommand>`

Default: `""`

#### maxRunningTime
Maximum time to run tests.

Default: `60 minutes`


#### testInOrder
Forces _Blinky_ to test each mutant in order, even if _Blinky_ estimates that
there is no time to test all mutants.

Default: `false`

#### timeoutFactor
see [timeout](#timeout).

Default: `1.5`

#### timeout

When _Blinky_ mutates code, it cannot determine whether a code mutation results
in an infinite loop (see [Halting problem](https://en.wikipedia.org/wiki/Halting_problem)).
In order to deal with mutants that create infinite loops, the test run is terminated 
after a certain period of time.
_Blinky_ will consider that the mutant was **killed** in a case of a timeout.
This period is configurable with two settings: timeout and timeoutFactor.
Formula used:
```
timeoutForEachTest = netTime * timeoutFactor + timeout
```
`netTime` is calculated during the initial test run (when no mutants are active).

Default: `1 second`

timeoutForTestRun = netTime * timeoutFactor + timeout

#### mutationMinimum
Minimum mutation score to fail (only useful if failOnMinimum is `true`).
Value must be between 0 and 100. Can have one decimal place of precision.

The mutation score is `mutations killed / total mutations tested`.
E.g. If we test `200` mutations and `90` are killed by the tests the mutation score will be `45.0`.

Default: `25.0`

#### failOnMinimum
Exits with non-zero code if the mutation score is below `mutationMinimum` value.

Default: `false`

#### onlyMutateDiff
If set, only mutates added and edited files in git diff against the master branch.

Default: false

#### mainBranch
Sets the main branch to compare against when using `onlyMutateDiff` option.

Default: main

#### multiRun
Only test the mutants of the given index, 1 <= job-index <= amount-of-jobs

This parameter helps running Blinky in parallel, useful to run Blinky in independent machines.
E.g. If you have two CI jobs that run Blinky on the same project and configuration, you can use:
```
# First CI job:
blinky .blinky.conf --multiRun 1/2

# Second CI job:
blinky .blinky.conf --multiRun 2/2
```
This makes each CI job run half the mutations without overlapping (i.e. testing the same mutant).
The first CI will test mutants 1,3,5,7,9,...
The second CI will test mutants 2,4,6,8,10,...

Format: <job-index>/<amount-of-jobs>  
Default: 1/1

#### mutant
Only test the mutants within the given indices.

Format: number/number-number
E.g:
3
4-8
10,20,30,45-58

Default: 1-2147483647

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
This configuration will allow all mutators except `LiteralBooleans`, `ArithmeticOperators.IntMulToDiv` and `ScalaOptions.OrElse`.
 
Default value: `[]`

---

Example of a simple `.blinky.conf` file:
```hocon
projectPath = "."
filesToMutate = "src"
options = {
  testRunner = "sbt"
  compileCommand = "Test/compile"
  testCommand = "test"
}
```

Example of a more custom configured `.blinky.conf` file:
```hocon
projectPath = "."
filesToMutate = "blinky-core/src"
filesToExclude = "**/internal/*scala"
options = {
  copyGitFolder = true
  verbose = false
  dryRun = false
  testRunner = "sbt"
  compileCommand = "Test/compile"
  testCommand = "test"
  maxRunningTime = 40 minutes
  failOnMinimum = true
  mutationMinimum = 50
  onlyMutateDiff = true
  mainBranch = "master"
  timeout = 5 seconds
  timeoutFactor = 2.0
  mutant = "1-20,50,73"
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
