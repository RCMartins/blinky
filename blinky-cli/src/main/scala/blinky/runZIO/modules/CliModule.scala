package blinky.runZIO.modules

import better.files.File
import zio.ZIO

trait CliModule {
  def cliModule: CliModule.Service[Any]
}

object CliModule {

  trait Service[R] {
    def pwd: ZIO[R, Nothing, File]
  }

  trait Live extends CliModule {
    val pwdLive: File

    override val cliModule: Service[Any] = new Service[Any] {
      override def pwd: ZIO[Any, Nothing, File] =
        ZIO.succeed(pwdLive)
    }
  }

  object factory extends CliModule.Service[CliModule] {
    override def pwd: ZIO[CliModule, Nothing, File] =
      ZIO.accessM[CliModule](_.cliModule.pwd)
  }

}
