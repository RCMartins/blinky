package blinky.run

sealed trait RunResult

object RunResult {

  case object MutantKilled extends RunResult
  case object MutantSurvived extends RunResult
  case object Timeout extends RunResult

}
