# Blinky

[![Build Status][badge-github-actions]][link-github-actions]
[![Coverage Status][badge-codecov]][link-codecov]
[![Sonatype Nexus (Releases)][badge-release]][link-release]
[![Sonatype Nexus (Snapshots)][badge-snapshot]][link-snapshot]
[![Scala Steward Badge][badge-scalasteward]][link-scalasteward]

Mutation testing is a type of software testing where we mutate (change) certain expressions in the source code 
and check if the test cases are able to find the errors.
It is a type of White Box Testing which is mainly used for Unit Testing.

_Blinky_ has 3 main steps:
* Copy the git project to a temporary folder (where the source code can be safely modified)
* Run the scalafix tool with the _Blinky_ rule (on the copy project)
* Run the project tests on the mutated code (usually with only 1 mutant active each time)

We use _Blinky_ to test this repository, improving the test code quality.

Similar projects:
* [scalamu](https://github.com/sugakandrey/scalamu)
* [stryker4s](https://github.com/stryker-mutator/stryker4s)

The main difference is that in _Blinky_ the mutations are semantic instead of just syntactic.
Meaning when using a rule like `ScalaOptions.filter` we only mutate calls to
the method `filter` of objects of type `scala.Option`.
In order to have this semantic information about the types _Blinky_ 
needs the [semanticdb](https://scalameta.org/docs/semanticdb/guide.html)
data of all files that we want to mutate.

### [User documentation][docs]
Head over to [the user docs][docs] for instructions on how to install blinky.

### Project maintenance
Create a new scalafix executable after upgrading scalafix dependency with:
```
coursier bootstrap ch.epfl.scala:scalafix-cli_2.13.8:0.9.34 -f --main scalafix.cli.Cli -o scalafix
```

[badge-github-actions]: https://github.com/RCMartins/blinky/actions/workflows/ci.yml/badge.svg?branch=master "build"
[badge-codecov]: https://codecov.io/gh/RCMartins/blinky/branch/master/graph/badge.svg?token=o3yIhzL932 "covecov"
[badge-release]: https://img.shields.io/nexus/r/com.github.rcmartins/blinky-cli_2.13?nexusVersion=3&server=https%3A%2F%2Foss.sonatype.org "release"
[badge-snapshot]: https://img.shields.io/nexus/s/com.github.rcmartins/blinky-cli_2.13?server=https%3A%2F%2Foss.sonatype.org "snapshot"
[badge-scalasteward]: https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII= "scala steward"

[link-github-actions]: https://github.com/RCMartins/blinky/actions "build"
[link-codecov]: https://codecov.io/gh/RCMartins/blinky "covecov"
[link-release]: https://search.maven.org/search?q=g:%22com.github.rcmartins%22%20and%20a:%22blinky_2.13%22 "release"
[link-snapshot]: https://oss.sonatype.org/content/repositories/snapshots/com/github/rcmartins/blinky-cli_2.13/ "snapshot"
[link-scalasteward]: https://scala-steward.org "scala steward"

[docs]: https://rcmartins.github.io/blinky/
