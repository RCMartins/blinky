package blinky.v0.mutators

import blinky.v0.Mutator.{MutationResult, default}
import blinky.v0.{Mutator, MutatorGroup}
import scalafix.v1.{SemanticDocument, SymbolMatcher, XtensionTreeScalafix}

import scala.meta.Term

object ArithmeticOperators extends MutatorGroup {
  override val groupName: String = "ArithmeticOperators"

  override val getSubMutators: List[Mutator] =
    List(
      IntMutators.IntPlusToMinus,
      IntMutators.IntMinusToPlus,
      IntMutators.IntMulToDiv,
      IntMutators.IntDivToMul,
      CharMutators.CharPlusToMinus,
      CharMutators.CharMinusToPlus,
      CharMutators.CharMulToDiv,
      CharMutators.CharDivToMul
    )

  private object IntMutators {

    val IntPlusToMinus: SimpleMutator =
      ArithmeticMutator("IntPlusToMinus", "+", "scala/Int#`+`(+4).", "-")
    val IntMinusToPlus: SimpleMutator =
      ArithmeticMutator("IntMinusToPlus", "-", "scala/Int#`-`(+3).", "+")
    val IntMulToDiv: SimpleMutator =
      ArithmeticMutator("IntMulToDiv", "*", "scala/Int#`*`(+3).", "/")
    val IntDivToMul: SimpleMutator =
      ArithmeticMutator("IntDivToMul", "/", "scala/Int#`/`(+3).", "*")

  }

  private object CharMutators {

    val CharPlusToMinus: SimpleMutator =
      ArithmeticMutator("CharPlusToMinus", "+", "scala/Char#`+`(+4).", "-")
    val CharMinusToPlus: SimpleMutator =
      ArithmeticMutator("CharMinusToPlus", "-", "scala/Char#`-`(+3).", "+")
    val CharMulToDiv: SimpleMutator =
      ArithmeticMutator("CharMulToDiv", "*", "scala/Char#`*`(+3).", "/")
    val CharDivToMul: SimpleMutator =
      ArithmeticMutator("CharDivToMul", "/", "scala/Char#`/`(+3).", "*")

  }

  private case class ArithmeticMutator(
      mutatorName: String,
      opName: String,
      symbolMatch: String,
      newOpName: String
  ) extends SimpleMutator(mutatorName) {
    override def getMutator(implicit doc: SemanticDocument): MutationResult = {
      case op @ Term.ApplyInfix(left, Term.Name(`opName`), targs, right)
          if SymbolMatcher.exact(symbolMatch).matches(op.symbol) =>
        default(Term.ApplyInfix(left, Term.Name(newOpName), targs, right))
    }
  }

}
