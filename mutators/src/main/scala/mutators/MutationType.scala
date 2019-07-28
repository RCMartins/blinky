package mutators

import metaconfig._
import scalafix.internal.config._
import scalafix.v1._

import scala.meta._

trait MutationType {

  def collectMutations(term: Term)(implicit doc: SemanticDocument): (Iterable[Term], Boolean)

}

object MutationType {
  val all: List[MutationType] = List(
    LiteralBoolean,
    ArithmeticOperators,
    ConditionalExpressions,
    LiteralString,
    ScalaOption
  )

  implicit val readerMutationType: ConfDecoder[MutationType] =
    ReaderUtil.fromMap(all.map(mType => mType.toString -> mType).toMap)

  case object LiteralBoolean extends MutationType {
    override def collectMutations(
        term: Term
    )(implicit doc: SemanticDocument): (Iterable[Term], Boolean) = term match {
      case Lit.Boolean(value) =>
        default(Lit.Boolean(!value))
      case _ =>
        empty
    }
  }

  case object ArithmeticOperators extends MutationType {
    override def collectMutations(
        term: Term
    )(implicit doc: SemanticDocument): (Iterable[Term], Boolean) = term match {
      case plus @ Term.ApplyInfix(left, Term.Name("+"), targs, right)
          if SymbolMatcher.exact("scala/Int#`+`(+4).").matches(plus.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("-"), targs, right))
      case minus @ Term.ApplyInfix(left, Term.Name("-"), targs, right)
          if SymbolMatcher.exact("scala/Int#`-`(+3).").matches(minus.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("+"), targs, right))
      case mult @ Term.ApplyInfix(left, Term.Name("*"), targs, right)
          if SymbolMatcher.exact("scala/Int#`*`(+3).").matches(mult.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("/"), targs, right))
      case div @ Term.ApplyInfix(left, Term.Name("/"), targs, right)
          if SymbolMatcher.exact("scala/Int#`/`(+3).").matches(div.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("*"), targs, right))
      case _ =>
        empty
    }
  }

  case object ConditionalExpressions extends MutationType {
    override def collectMutations(
        term: Term
    )(implicit doc: SemanticDocument): (Iterable[Term], Boolean) = term match {
      case and @ Term.ApplyInfix(left, Term.Name("&&"), targs, right)
          if SymbolMatcher.exact("scala/Boolean#`&&`().").matches(and.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("||"), targs, right))
      case or @ Term.ApplyInfix(left, Term.Name("||"), targs, right)
          if SymbolMatcher.exact("scala/Boolean#`||`().").matches(or.symbol) =>
        default(Term.ApplyInfix(left, Term.Name("&&"), targs, right))
      case boolNeg @ Term.ApplyUnary(Term.Name("!"), arg)
          if SymbolMatcher.exact("scala/Boolean#`unary_!`().").matches(boolNeg.symbol) =>
        default(arg)
      case _ =>
        empty
    }
  }

  case object LiteralString extends MutationType {
    override def collectMutations(
        term: Term
    )(implicit doc: SemanticDocument): (Iterable[Term], Boolean) = term match {
      case Lit.String(value) if value.isEmpty =>
        default(Lit.String("mutated!"))
      case Lit.String(value) if value.nonEmpty =>
        default(Lit.String(""), Lit.String("mutated!"))
      case concat @ Term.ApplyInfix(_, Term.Name("+"), _, _)
          if SymbolMatcher.exact("java/lang/String#`+`().").matches(concat.symbol) =>
        //List(left, right, Lit.String("mutated!"), Lit.String(""))
        fullReplace(Lit.String("mutated!"), Lit.String(""))
      case _ =>
        empty
    }
  }

  case object ScalaOption extends MutationType {
    override def collectMutations(
        term: Term
    )(implicit doc: SemanticDocument): (Iterable[Term], Boolean) = {
      term match {
        case getOrElse @ Term.Apply(Term.Select(_, Term.Name("getOrElse")), List(arg))
            if SymbolMatcher.exact("scala/Option#getOrElse().").matches(getOrElse.symbol) =>
          default(arg)
        case exists @ Term.Apply(Term.Select(termName, Term.Name("exists")), args)
            if SymbolMatcher.exact("scala/Option#exists().").matches(exists.symbol) =>
          default(Term.Apply(Term.Select(termName, Term.Name("forall")), args))
        case forall @ Term.Apply(Term.Select(termName, Term.Name("forall")), args)
            if SymbolMatcher.exact("scala/Option#forall().").matches(forall.symbol) =>
          default(Term.Apply(Term.Select(termName, Term.Name("exists")), args))
        case isEmpty @ Term.Select(termName, Term.Name("isEmpty") | Term.Name("isDefined"))
            if SymbolMatcher.exact("scala/Option#isDefined().").matches(isEmpty.symbol) ||
              SymbolMatcher.exact("scala/Option#isEmpty().").matches(isEmpty.symbol) =>
          default(Term.Select(termName, Term.Name("nonEmpty")))
        case nonEmpty @ Term.Select(termName, Term.Name("nonEmpty"))
            if SymbolMatcher.exact("scala/Option#nonEmpty().").matches(nonEmpty.symbol) =>
          default(Term.Select(termName, Term.Name("isEmpty")))
        case fold @ Term.Apply(Term.Apply(Term.Select(_, Term.Name("fold")), List(argDefault)), _)
            if SymbolMatcher.exact("scala/Option#fold().").matches(fold.symbol) =>
          default(argDefault)
        case orElse @ Term.Apply(Term.Select(termName, Term.Name("orElse")), List(arg))
            if SymbolMatcher.exact("scala/Option#orElse().").matches(orElse.symbol) =>
          default(termName, arg)
        case orNull @ Term.Select(_, Term.Name("orNull"))
            if SymbolMatcher.exact("scala/Option#orNull().").matches(orNull.symbol) =>
          default(Lit.Null())
        case filter @ Term.Apply(Term.Select(termName, Term.Name("filter")), args)
            if SymbolMatcher.exact("scala/Option#filter().").matches(filter.symbol) =>
          default(termName, Term.Apply(Term.Select(termName, Term.Name("filterNot")), args))
        case filterNot @ Term.Apply(Term.Select(termName, Term.Name("filterNot")), args)
            if SymbolMatcher.exact("scala/Option#filterNot().").matches(filterNot.symbol) =>
          default(termName, Term.Apply(Term.Select(termName, Term.Name("filter")), args))
        case contains @ Term.Apply(Term.Select(_, Term.Name("contains")), _)
            if SymbolMatcher.exact("scala/Option#contains().").matches(contains.symbol) =>
          default(Lit.Boolean(true), Lit.Boolean(false))
        case _ =>
          empty
      }
    }
  }

  def default(terms: Term*): (List[Term], Boolean) = (terms.toList, false)

  def fullReplace(terms: Term*): (List[Term], Boolean) = (terms.toList, true)

  def empty: (List[Term], Boolean) = (List.empty, false)
}
