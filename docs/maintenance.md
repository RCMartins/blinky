---
id: maintenance
title: Project Maintenance
sidebar_label: Project Maintenance
---

To debug an arbitrary expression, run on an ammonite with `amm --thin` command
```scala
import $ivy.`org.scalameta::scalameta:4.7.7`, scala.meta._
"""List("a", "b", "c").reduceOption(_ + _)""".parse[Term].get.structure // prints the AST structure
show("""List("a", "b", "c").reduceOption(_ + _)""".parse[Term].get, 60) // prints the AST in a more readable format
```
This will print the AST of the expression, which you can then use to figure out
what's the expected match pattern.
