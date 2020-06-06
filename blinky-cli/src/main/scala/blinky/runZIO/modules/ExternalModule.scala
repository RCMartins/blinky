package blinky.runZIO.modules

import blinky.runZIO.external.{AmmoniteExternalCalls, ExternalCalls}
import zio.ZIO

trait ExternalModule {
  def externalModule: ExternalModule.Service[Any]
}

object ExternalModule {

  trait Service[R] {
    def external: ZIO[R, Nothing, ExternalCalls]
  }

  trait Live extends ExternalModule {
    override def externalModule: Service[Any] =
      new Service[Any] {
        override def external: ZIO[Any, Nothing, ExternalCalls] =
          ZIO.succeed(AmmoniteExternalCalls)
      }
  }

  object factory extends ExternalModule.Service[ExternalModule] {
    override def external: ZIO[ExternalModule, Nothing, ExternalCalls] =
      ZIO.accessM[ExternalModule](_.externalModule.external)
  }

}
