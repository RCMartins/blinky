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

#### multiRun
Only test the mutants of the given index, 1 <= job-index <= number-of-jobs

This parameter helps running Blinky in parallel, useful to run Blinky in independent machines.
E.g. If you have two Travis jobs that run Blinky on the same project and configuration, you can use:
```
# First Travis job:
blinky .blinky.conf --multiRun 1/2

# Second Travis job:
blinky .blinky.conf --multiRun 2/2
```
This makes each travis job run half the mutations without overlapping (i.e. testing the same mutant).

Format: <job-index>/<number-of-jobs>  
Default: 1/1

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
