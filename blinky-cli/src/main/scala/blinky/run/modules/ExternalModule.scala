package blinky.run.modules

import blinky.run.external.{ExternalCalls, OSExternalCalls}
import zio.{Layer, ZIO, ZLayer}

object ExternalModule {

  trait Service {
    def external: ZIO[Any, Nothing, ExternalCalls]
  }

  def external: ZIO[ExternalModule, Nothing, ExternalCalls] =
    ZIO.accessM[ExternalModule](_.get.external)

  val live: Layer[Nothing, ExternalModule] =
    ZLayer.succeed(new Service {
      override def external: ZIO[Any, Nothing, ExternalCalls] =
        ZIO.succeed(OSExternalCalls)
    })

}
