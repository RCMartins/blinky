package blinky.v0

import blinky.v0.Mutator._
import blinky.v0.ReplaceType._
import scalafix.v1._

import scala.meta._

trait MutatorGroup {
  def groupName: String

  def getSubMutators: List[Mutator]

  abstract class SimpleMutator(simpleName: String) extends Mutator {
    override val name = s"$groupName.$simpleName"
  }
}

trait Mutator {
  def name: String

  def getMutator(implicit doc: SemanticDocument): MutationResult
}

object Mutator {
  abstract class NonGroupedMutator(override val name: String) extends Mutator

  type MutationResult = PartialFunction[Term, ReplaceType]

  private val allGroups: List[MutatorGroup] =
    List(
      mutators.ArithmeticOperators,
      mutators.ConditionalExpressions,
      mutators.LiteralStrings,
      mutators.ScalaOptions,
      mutators.ScalaTry,
      mutators.Collections,
      mutators.PartialFunctions,
      mutators.ScalaStrings,
      mutators.ControlFlow
    )

  val all: Map[String, Mutator] =
    Map(
      LiteralBooleans.name -> LiteralBooleans
    ) ++
      allGroups.flatMap(group => group.getSubMutators.map(mutator => (mutator.name, mutator)))

  def findMutators(str: String): List[Mutator] =
    all.collect {
      case (name, mutation) if name == str                => mutation
      case (name, mutation) if name.startsWith(str + ".") => mutation
    }.toList

  object LiteralBooleans extends NonGroupedMutator("LiteralBooleans") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Lit.Boolean(value) =>
        default(Lit.Boolean(!value))
    }
  }

  def default(terms: Term*): ReplaceType = Standard(terms.toList)

  def fullReplace(terms: Term*): ReplaceType = FullReplace(terms.toList)

}
