/*
rule = Blinky
Blinky.filesToMutate = [all]
Blinky.enabledMutators = [ZIO]
 */
package test.zio

import zio._

object ZIOForYield2 {
  val zio1: IO[Throwable, Int] = ZIO.succeed(1)
  val zio2: Task[Int] = ZIO.succeed(1)
  val zio3: RIO[Int, Int] = ZIO.service[Int]
  val zio4: UIO[Int] = ZIO.succeed(1)
  val zio5: URIO[Int, Int] = ZIO.service[Int]

  val zioAll: ZIO[Int, Throwable, Unit] =
    for {
      _ <- zio1
      _ <- zio2
      _ <- zio3
      _ <- zio4
      _ <- zio5
    } yield ()

}
