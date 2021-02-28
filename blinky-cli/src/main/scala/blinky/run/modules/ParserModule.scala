package blinky.run.modules

import scopt.{DefaultOEffectSetup, OEffectSetup}
import zio.{Layer, ZIO, ZLayer}

object ParserModule {

  trait Service {
    def parser: ZIO[Any, Nothing, OEffectSetup]
  }

  def parser: ZIO[ParserModule, Nothing, OEffectSetup] =
    ZIO.accessM[ParserModule](_.get.parser)

  val live: Layer[Nothing, ParserModule] =
    ZLayer.succeed(new Service {
      override def parser: ZIO[Any, Nothing, OEffectSetup] =
        ZIO.succeed(new DefaultOEffectSetup {})
    })

}
