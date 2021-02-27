package blinky.run.modules

import scopt.{DefaultOParserSetup, OParserSetup}
import zio.{Layer, ZIO, ZLayer}

object ParserModule {

  trait Service {
    def parser: ZIO[Any, Nothing, OParserSetup]
  }

  def parser: ZIO[ParserModule, Nothing, OParserSetup] =
    ZIO.accessM[ParserModule](_.get.parser)

  val live: Layer[Nothing, ParserModule] =
    ZLayer.succeed(new Service {
      override def parser: ZIO[Any, Nothing, OParserSetup] =
        ZIO.succeed(new DefaultOParserSetup() {})
    })

}
