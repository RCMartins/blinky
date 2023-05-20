/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ZIO]
 */
package test.zio

import zio.ZIO

object ZIOConditionalFilters {
  val task1: ZIO[Any, Nothing, String] = ZIO.succeed("abc")
  val task2: ZIO[Any, Nothing, Option[String]] = task1.unless(true)
  val task3: ZIO[Any, Nothing, Option[String]] = task1.when(true)
}
