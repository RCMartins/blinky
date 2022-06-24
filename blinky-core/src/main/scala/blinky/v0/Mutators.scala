package blinky.v0

import blinky.v0
import metaconfig.{Conf, ConfDecoder, ConfError, Configured}

case class Mutators(mutations: List[Mutator])

object Mutators {
  val all: Mutators = Mutators(Mutator.all.values.toList)

  implicit val readerMutations: ConfDecoder[Mutators] = {
    def readerMutationsAux(path: String): ConfDecoder[Mutators] =
      ConfDecoder.from[Mutators] {
        case Conf.Str(mutatorName) =>
          Mutator.findMutators(path + mutatorName) match {
            case Nil  => Configured.notOk(ConfError.message(s"$path$mutatorName was not found!"))
            case list => Configured.Ok(v0.Mutators(list))
          }
        case Conf.Lst(values) =>
          val list = values.map(readerMutationsAux(path).read)
          list
            .find(_.isNotOk)
            .getOrElse(Configured.Ok(Mutators(list.flatMap(_.get.mutations))))
        case Conf.Obj(values) =>
          val list =
            values.map { case (mutatorName, conf) =>
              readerMutationsAux(s"$mutatorName.").read(conf)
            }
          list
            .find(_.isNotOk)
            .getOrElse(Configured.Ok(Mutators(list.flatMap(_.get.mutations))))
        case other =>
          Configured.typeMismatch("String with a Mutator name", other)
      }

    readerMutationsAux("")
  }
}
