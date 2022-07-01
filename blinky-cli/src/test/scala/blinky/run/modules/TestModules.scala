package blinky.run.modules

import better.files.File
import blinky.run.external.ExternalCalls
import scopt.OEffectSetup
import zio.{Layer, ZIO, ZLayer}

object TestModules {

  def testParserModule(oEffectSetup: OEffectSetup): Layer[Nothing, ParserModule] =
    ZLayer.succeed(
      new ParserModule {
        override def parser: ZIO[Any, Nothing, OEffectSetup] =
          ZIO.succeed(oEffectSetup)
      }
    )

  def testCliModule(pwdLive: File): Layer[Nothing, CliModule] =
    ZLayer.succeed(
      new CliModule {
        override def pwd: ZIO[Any, Nothing, File] =
          ZIO.succeed(pwdLive)
      }
    )

  def testExternalModule(externalCalls: ExternalCalls): Layer[Nothing, ExternalModule] =
    ZLayer.succeed(
      new ExternalModule {
        override def external: ZIO[Any, Nothing, ExternalCalls] =
          ZIO.succeed(externalCalls)
      }
    )

}
