---
id: maintenance
title: Project Maintenance
sidebar_label: Project Maintenance
---

To run the tests locally, use `sbt test`.
Sometimes it's necessary to run `sbt` with more memory available, use `sbt -J-Xmx3G test` instead.

To debug an arbitrary expression, run on an ammonite shell with `amm --thin` command
```scala
import $ivy.`org.scalameta::scalameta:4.7.8`, scala.meta._
"""List("a", "b", "c").reduceOption(_ + _)""".parse[Term].get.structure // prints the AST structure
show("""List("a", "b", "c").reduceOption(_ + _)""".parse[Term].get, 60) // prints the AST in a more readable format
```
This will print the AST of the expression, which you can then use to figure out
what's the expected match pattern.

To use a snapshot version of `scalameta` use:
```scala
interp.repositories() ++= Seq(
  coursierapi.MavenRepository.of("https://oss.sonatype.org/content/repositories/snapshots/")
)
import $ivy.`org.scalameta::scalameta:4.7.8+11-00bc83ad-SNAPSHOT`, scala.meta._
```
