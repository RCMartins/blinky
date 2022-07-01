package blinky.run.modules

import better.files.File
import zio.{Layer, ZIO, ZLayer}

trait CliModule {
  def pwd: ZIO[Any, Nothing, File]
}

object CliModule {

  def layer(pwdLive: File): Layer[Nothing, CliModule] =
    ZLayer.succeed(
      new CliModule {
        override def pwd: ZIO[Any, Nothing, File] =
          ZIO.succeed(pwdLive)
      }
    )

}
