package blinky.run.modules

import better.files.File
import zio.{Layer, ZIO, ZLayer}

object CliModule {

  trait Service {
    def pwd: ZIO[Any, Nothing, File]
  }

  def pwd: ZIO[CliModule, Nothing, File] =
    ZIO.accessM[CliModule](_.get.pwd)

  def live(pwdLive: File): Layer[Nothing, CliModule] =
    ZLayer.succeed(new Service {
      override def pwd: ZIO[Any, Nothing, File] =
        ZIO.succeed(pwdLive)
    })

}
