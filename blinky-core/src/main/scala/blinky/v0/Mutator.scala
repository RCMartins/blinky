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

  type MutationResult = PartialFunction[Term, ReplaceType]

  type MutationSimpleResult = PartialFunction[Term, List[Term]]

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
      mutators.ControlFlow,
      mutators.ZIO,
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

  private abstract class NonGroupedMutator(override val name: String) extends Mutator

  private object LiteralBooleans extends NonGroupedMutator("LiteralBooleans") {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case Lit.Boolean(value) =>
        default(Lit.Boolean(!value))
    }
  }

  def default(terms: Term*): ReplaceType = Standard(terms.toList)

  def default(terms: List[Term]): ReplaceType = Standard(terms)

  def fullReplace(terms: Term*): ReplaceType = FullReplace(terms.toList)

  def getSymbolType(term: Term)(implicit doc: SemanticDocument): Option[Symbol] =
    term.symbol.info.flatMap {
      _.signature match {
        case ValueSignature(TypeRef(_, symbol, _))        => Some(symbol)
        case MethodSignature(_, _, TypeRef(_, symbol, _)) => Some(symbol)
        case TypeSignature(_, TypeRef(_, symbol, _), _)   => Some(symbol)
        case _                                            => None
      }
    }

}
