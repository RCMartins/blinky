package blinky.run

import zio.Has

package object modules {

  type ParserModule = Has[ParserModule.Service]

  type CliModule = Has[CliModule.Service]

  type ExternalModule = Has[ExternalModule.Service]

}
