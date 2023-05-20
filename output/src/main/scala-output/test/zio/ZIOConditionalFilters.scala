package test.zio

import zio.ZIO

object ZIOConditionalFilters {
  val task1: ZIO[Any, Nothing, String] = ZIO.succeed("abc")
  val task2: ZIO[Any, Nothing, Option[String]] = if (???) task1.when(true) else if (???) task1.asSome else if (???) task1.as(None) else task1.unless(true)
  val task3: ZIO[Any, Nothing, Option[String]] = if (???) task1.unless(true) else if (???) task1.asSome else if (???) task1.as(None) else task1.when(true)
}
