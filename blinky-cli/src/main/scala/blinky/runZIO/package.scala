package blinky

import ammonite.ops.Path
import blinky.runZIO.modules.{CliModule, ExternalModule, ParserModule}
import zio.ExitCode

package object runZIO {

  type FullEnvironment = ParserModule with ExternalModule with CliModule

  type InstructionType = Instruction[ExitCode, Path]

}
