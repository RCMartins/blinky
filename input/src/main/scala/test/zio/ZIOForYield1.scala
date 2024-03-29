/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ZIO]
 */
package test.zio

import zio.ZIO

object ZIOForYield1 {
  val task1: ZIO[Int, Nothing, Int] = ZIO.service[Int]
  val task2: ZIO[String, Nothing, String] = ZIO.service[String]
  val task3: ZIO[Any, Nothing, Int] = ZIO.succeed(1)

  val task4a: ZIO[String with Int, Nothing, Unit] =
    for {
      a <- task1
      b <- task2
    } yield ()

  val task4b: ZIO[String with Int, Nothing, Int] =
    for {
      a <- task1
      _ <- task2
    } yield a

  val task4c: ZIO[String with Int, Nothing, Int] =
    for {
      _ <- task1
      _ <- task2
    } yield 1 + 1

  val task4d: ZIO[Int, Nothing, Int] =
    for {
      a <- task1
    } yield a

  val task4e: ZIO[Int, Nothing, Int] =
    for {
      _ <- task1
    } yield 1

  val task4f: ZIO[Int, Nothing, Int] =
    for {
      _ <- task1
      v = 1
      _ <- task3
      _ <- task1
    } yield 1

  val task4g: ZIO[Int, Nothing, Int] =
    for {
      _ <- task1
      v = 1
      _ <- task3
      t = 1
      _ <- task1
    } yield 1

  val nonZIO: Option[Int] =
    for {
      _ <- Some(1)
      _ <- Some(2)
    } yield 3
}
