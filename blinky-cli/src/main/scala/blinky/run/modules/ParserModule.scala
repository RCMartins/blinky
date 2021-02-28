package blinky.run.modules

import scopt.{DefaultOEffectSetup, OEffectSetup}
import zio.ZIO

trait ParserModule {
  def parserModule: ParserModule.Service[Any]
}

object ParserModule {

  trait Service[R] {
    def parser: ZIO[R, Nothing, OEffectSetup]
  }

  trait Live extends ParserModule {
    override def parserModule: Service[Any] =
      new Service[Any] {
        override def parser: ZIO[Any, Nothing, OEffectSetup] =
          ZIO.succeed(new DefaultOEffectSetup {})
      }
  }

  object factory extends ParserModule.Service[ParserModule] {
    override def parser: ZIO[ParserModule, Nothing, OEffectSetup] =
      ZIO.accessM[ParserModule](_.parserModule.parser)
  }

}
