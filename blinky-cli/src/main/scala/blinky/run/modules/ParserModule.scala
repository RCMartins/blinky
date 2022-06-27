package blinky.run.modules

import scopt.{DefaultOEffectSetup, OEffectSetup}
import zio.{Layer, ZIO, ZLayer}

trait ParserModule {
  def parser: ZIO[Any, Nothing, OEffectSetup]
}

object ParserModule {

  val layer: Layer[Nothing, ParserModule] =
    ZLayer.succeed(
      new ParserModule {
        override def parser: ZIO[Any, Nothing, OEffectSetup] =
          ZIO.succeed(new DefaultOEffectSetup {})
      }
    )

}
