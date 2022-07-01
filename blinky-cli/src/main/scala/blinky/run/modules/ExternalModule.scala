package blinky.run.modules

import blinky.run.external.{ExternalCalls, OSExternalCalls}
import zio.{Layer, ZIO, ZLayer}

trait ExternalModule {
  def external: ZIO[Any, Nothing, ExternalCalls]
}

object ExternalModule {

  val layer: Layer[Nothing, ExternalModule] =
    ZLayer.succeed(
      new ExternalModule {
        override def external: ZIO[Any, Nothing, ExternalCalls] =
          ZIO.succeed(OSExternalCalls)
      }
    )

}
