package test.zio

import zio.ZIO

object ZIOForYield {
  val task1: ZIO[Int, Nothing, Int] = ZIO.service[Int]
  val task2: ZIO[String, Nothing, String] = ZIO.service[String]
  val task3: ZIO[Any, Nothing, Int] = ZIO.succeed(1)

  val task4a: ZIO[String with Int, Nothing, Unit] =
    if (???) zio.ZIO.unit ///
        else for (a <- task1; b <- task2) yield ()

  val task4b: ZIO[String with Int, Nothing, Int] =
    if (???) for (a <- task1) yield a ///
        else for (a <- task1; _ <- task2) yield a

  val task4c: ZIO[Int, Nothing, Int] =
    for {
      a <- task1
    } yield a

  val task4d: ZIO[Int, Nothing, Int] =
    if (???) zio.ZIO.succeed(1) ///
else if (???) for (_ <- task1; v = 1; _ <- task1) yield 1 ///
else if (???) for (_ <- task1; v = 1; _ <- task3) yield 1 ///
        else for (_ <- task1; v = 1; _ <- task3; _ <- task1) yield 1

  val task4e: ZIO[Int, Nothing, Int] =
    if (???) zio.ZIO.succeed(1) ///
    else if (???) for (_ <- task1; v = 1; t = 1; _ <- task1) yield 1 ///
    else if (???) for (_ <- task1; v = 1; _ <- task3; t = 1) yield 1 ///
    else for (_ <- task1; v = 1; _ <- task3; t = 1; _ <- task1) yield 1
}
