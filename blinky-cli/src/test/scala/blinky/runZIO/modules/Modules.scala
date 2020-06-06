package blinky.runZIO.modules

import better.files.File
import blinky.runZIO.external.ExternalCalls
import scopt.OParserSetup
import zio.ZIO

object Modules {

  class TestParserModule(oParserSetup: OParserSetup) extends ParserModule.Service[Any] {
    override def parser: ZIO[Any, Nothing, OParserSetup] =
      ZIO.succeed(oParserSetup)
  }

  class TestExternalModule(externalCalls: ExternalCalls) extends ExternalModule.Service[Any] {
    override def external: ZIO[Any, Nothing, ExternalCalls] =
      ZIO.succeed(externalCalls)
  }

  class TestCliModule(pwdFile: File) extends CliModule.Service[Any] {
    override def pwd: ZIO[Any, Nothing, File] =
      ZIO.succeed(pwdFile)
  }

}
