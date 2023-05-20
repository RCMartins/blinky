package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.SemanticDocument

import scala.meta.Term

object ControlFlow extends MutatorGroup {
  override val groupName: String = "ControlFlow"

  override val getSubMutators: List[Mutator] =
    List(
      IfMutator
    )

  private object IfMutator extends SimpleMutator("If") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Term.If(_, thenTerm, elseTerm) =>
        default(thenTerm, elseTerm)
    }
  }

}
