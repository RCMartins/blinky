package blinky.run.modules

import scopt.{DefaultOParserSetup, OParserSetup}
import zio.ZIO

trait ParserModule {
  def parserModule: ParserModule.Service[Any]
}

object ParserModule {

  trait Service[R] {
    def parser: ZIO[R, Nothing, OParserSetup]
  }

  trait Live extends ParserModule {
    override def parserModule: Service[Any] =
      new Service[Any] {
        override def parser: ZIO[Any, Nothing, OParserSetup] =
          ZIO.succeed(new DefaultOParserSetup() {})
      }
  }

  object factory extends ParserModule.Service[ParserModule] {
    override def parser: ZIO[ParserModule, Nothing, OParserSetup] =
      ZIO.accessM[ParserModule](_.parserModule.parser)
  }

}
