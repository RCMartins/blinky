package blinky.run

sealed trait TimeoutResult

object TimeoutResult {

  case object Ok extends TimeoutResult

  case object Timeout extends TimeoutResult

}
