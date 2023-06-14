package blinky.run.config

import metaconfig.{ConfDecoder, Configured}

sealed trait TestRunnerType {
  val key: String
}

object TestRunnerType {

  case object SBT extends TestRunnerType { val key: String = "sbt" }

  case object Bloop extends TestRunnerType { val key: String = "bloop" }

  implicit val decoder: ConfDecoder[TestRunnerType] =
    ConfDecoder.stringConfDecoder.flatMap { str =>
      str.toLowerCase match {
        case SBT.key   => Configured.Ok(TestRunnerType.SBT)
        case Bloop.key => Configured.Ok(TestRunnerType.Bloop)
        case _         => Configured.error("Invalid runner type. Should be 'sbt' or 'bloop'.")
      }
    }

}
