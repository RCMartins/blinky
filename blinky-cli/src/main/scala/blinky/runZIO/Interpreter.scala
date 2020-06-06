package blinky.runZIO

import scala.annotation.tailrec

object Interpreter {

  @tailrec
  def interpreter[A, P](program: Instruction[A, P]): A =
    program match {
      case Instruction.Result(value) =>
        value
      case Instruction.PrintLine(line, rest) =>
        Console.out.println(line)
        interpreter(rest)
      case Instruction.PrintErrorLine(line, rest) =>
        Console.err.println(line)
        interpreter(rest)
      case Instruction.ExternalRunSync(op @ _, args, path, rest) =>
        ???
    }

}
