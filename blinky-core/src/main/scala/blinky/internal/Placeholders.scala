package blinky.internal

import blinky.internal.MutatedTerms.{PlaceholderMutatedTerms, StandardMutatedTerms}

import scala.meta.Term
import scala.meta.Term._

class Placeholders(nextRandomName: () => Name) {

  def replacePlaceholders(
      original: Term,
      placeholderLocation: Option[Term],
      mutatedTerms: MutatedTerms,
      inApplyPart: Boolean
  ): Option[(Term, MutatedTerms)] =
    (original, mutatedTerms) match {
      case (Placeholder(), _) =>
        None
      case (original, mutatedTerms: StandardMutatedTerms)
          if original.collect { case Placeholder() => }.nonEmpty =>
        val (placeholderMode, originalReplaced, mutantsReplaced, vars) =
          replaceAllPlaceholders(original, mutatedTerms.mutated, inApplyPart)

        val placeholderFunction = generatePlaceholderFunction(vars)

//        println(
//          PlaceholderMutatedTerms(
//            originalReplaced,
//            placeholderFunction,
//            mutantsReplaced,
//            vars.map(_.value),
//            placeholderLocation,
//            mutatedTerms.needsParens
//          )
//        )

        Some(
          (
            original,
            if (placeholderMode)
              PlaceholderMutatedTerms(
                originalReplaced,
                placeholderFunction,
                mutantsReplaced,
                vars.map(_.value),
                placeholderLocation,
                mutatedTerms.needsParens
              )
            else
              mutatedTerms
          )
        )
      case other =>
        Some(other)
    }

  private def replaceAllPlaceholders(
      originalTerm: Term,
      initialMutants: Seq[Term],
      inApplyPart: Boolean
  ): (Boolean, Term, Seq[(Term, Term)], List[Name]) = {

    val amountOfPlaceholders: Int = countPlaceholders(originalTerm, inApplyPart = inApplyPart)

    if (amountOfPlaceholders == 0) {
      (
        false,
        originalTerm,
        Nil,
        Nil
      )
    } else {
      val newVars: List[Name] = List.fill(amountOfPlaceholders)(nextRandomName())

//      println()
//      println("/" * 50)
//      println("   " + originalTerm)
//      println("   " + "inApplyPart=" + inApplyPart)
//      println("   " + amountOfPlaceholders)
//      println("   " + newVars)

      val originalReplaced =
        replaceAllPlaceholders(originalTerm, newVars, inApplyPart)

//      println("-> " + originalReplaced)
//      println("=" * 50)
//      println(initialMutants)

      val mutatedReplacedOriginal: Seq[(Term, Term)] =
        initialMutants.map { term =>
          (term, replaceAllPlaceholders(term, newVars, inApplyPart))
        }

      val mutatedReplaced =
        mutatedReplacedOriginal.map { case (withP, withoutP) =>
          val amountOfPlaceholdersTerm = countPlaceholders(withP, inApplyPart = inApplyPart)
//          println((amountOfPlaceholders, amountOfPlaceholdersTerm))
          val remainingPlaceholders = amountOfPlaceholders - amountOfPlaceholdersTerm
          if (remainingPlaceholders == 0)
//            withP match {
//              case Placeholder() =>
//                (Name("identity"), withoutP)
//              case other         =>
//                (other, withoutP)
//            }
            (withP, withoutP)
          else
            (defaultPlaceholderFunction(remainingPlaceholders)(withP), withoutP)
        }

//      println(mutatedReplacedOriginal)
//      println(mutatedReplaced)
//      println("\\" * 50)

      (
        newVars.nonEmpty,
        originalReplaced,
        mutatedReplaced,
        newVars
      )
    }

//    if (anyMutantsReplaced)
//      loop(
//        placeholderMode = true,
//        originalReplaced,
//        mutatedReplaced,
//        newVar :: vars
//      )
//    else if (originalWasReplaced)
//      (
//        true,
//        originalReplaced,
//        mutatedReplaced,
//        newVar :: vars
//      )
//    else
//      (
//        placeholderMode,
//        originalTerm,
//        finalMutants,
//        vars.reverse
//      )

//    @tailrec
//    def loop(
//        placeholderMode: Boolean,
//        originalTerm: Term,
//        finalMutants: Seq[(Term, Term)],
//        vars: List[Name]
//    ): (Boolean, Term, Seq[(Term, Term)], List[Name]) = {

//      val newVar = Name(s"blinky_${UUID.randomUUID().toString}")

//      val mutatedReplacedOriginal: Seq[(Term, (Term, Boolean))] =
//        finalMutants.map { case (termO, termP) =>
//          (termO, replaceFirstPlaceholder(termP, newVar))
//        }
//      val anyMutantsReplaced =
//        mutatedReplacedOriginal.exists { case (_, (_, mutantReplaced)) => mutantReplaced }
//      val (originalReplaced, originalWasReplaced) =
//        replaceFirstPlaceholder(originalTerm, newVar)
//
//      println("$" * 50)
//      println(originalTerm)
//      println(originalReplaced)
//      println(originalWasReplaced)
//      println("-" * 50)
//
//      val mutatedReplaced =
//        mutatedReplacedOriginal.map {
//          case (withP, (withoutP, true)) =>
//            (withP, withoutP)
//          case (withP, (withoutP, false)) =>
//            val amountOfPlaceholders = countPlaceholders(originalTerm)
//            println((amountOfPlaceholders, originalTerm))
//            (defaultPlaceholderFunction(amountOfPlaceholders)(withP), withoutP)
//        }
//
//      println(finalMutants)
//      println(mutatedReplacedOriginal)
//      println(mutatedReplaced)
//      println(anyMutantsReplaced)
//      println("$" * 50)
//
//      if (anyMutantsReplaced)
//        loop(
//          placeholderMode = true,
//          originalReplaced,
//          mutatedReplaced,
//          newVar :: vars
//        )
//      else if (originalWasReplaced)
//        (
//          true,
//          originalReplaced,
//          mutatedReplaced,
//          newVar :: vars
//        )
//      else
//        (
//          placeholderMode,
//          originalTerm,
//          finalMutants,
//          vars.reverse
//        )
//    }

//    loop(
//      placeholderMode = false,
//      originalTerm,
//      initialMutants.map(term => (term, term)),
//      Nil
//    )
  }

  private def defaultPlaceholderFunction(amountOfPlaceholders: Int): Term => Term =
    (body: Term) => {
      Function(List.fill(amountOfPlaceholders)(Param(Nil, meta.Name.Anonymous(), None, None)), body)
    }

  private def generatePlaceholderFunction(newVars: List[Name]): Term => Term =
    (body: Term) => Term.Function(newVars.map(Term.Param(Nil, _, None, None)), body)

  private def replaceAllPlaceholders(
      initialTerm: Term,
      initialNewVars: List[Name],
      initialInApplyPart: Boolean
  ): Term = {
//    println("-" * 100)
//    println(initialTerm)
//    println((countPlaceholders(initialTerm), initialNewVars.size))
//    assert(countPlaceholders(initialTerm) == initialNewVars.size, initialTerm.structure)

    def replace(term: Term, newVars: List[Name], inApplyPart: Boolean): (Term, List[Name]) =
//      println("#" * 20)
//      println(term)
//      println(term.structure)
//      println("#" * 20)
      term match {
        case Placeholder() =>
          (newVars.head, newVars.tail)
        case ApplyInfix(lhs, op, targs, args) =>
          val (lhsReplaced, newVarsUpdated) = replace(lhs, newVars, inApplyPart = false)
          val (argsUpdated, newVarsFinal) =
//            if ((initialTerm eq term) || args.size > 1)
//              args.foldLeft((List.empty[Term], newVarsUpdated)) {
//                case ((argsUpdated, newVarsUpdated), Placeholder()) =>
//                  val (argUpdated, newVarsUpdated2) = replace(Placeholder(), newVarsUpdated)
//                  (argUpdated :: argsUpdated, newVarsUpdated2)
//                case ((argsUpdated, newVarsUpdated), arg) =>
//                  (arg :: argsUpdated, newVarsUpdated)
//              }
//            else {
//              val (argUpdated, newVarsUpdated2) = replace(args.head, newVarsUpdated)
//              (List(argUpdated), newVarsUpdated2)
//            }
            if (inApplyPart)
              (args, newVarsUpdated)
            else {
//              println("!" * 50)
//              println(term)
//              println("!" * 50)
              args match {
                case List(oneArg) =>
                  val (argUpdated, newVarsUpdated2) =
                    replace(oneArg, newVarsUpdated, inApplyPart = false)
                  (List(argUpdated), newVarsUpdated2)
                case _ =>
//                  (args, newVarsUpdated)
                  args.foldLeft((List.empty[Term], newVarsUpdated)) {
                    case ((argsUpdated, newVarsUpdated), Placeholder()) =>
                      val (argUpdated, newVarsUpdated2) =
                        replace(Placeholder(), newVarsUpdated, inApplyPart = false)
                      (argUpdated :: argsUpdated, newVarsUpdated2)
                    case ((argsUpdated, newVarsUpdated), arg) =>
                      (arg :: argsUpdated, newVarsUpdated)
                  }
              }
            }
//          args match {
//            case List(oneArg) =>
//              println(List(oneArg))
//              val (argUpdated, newVarsUpdated2) = replace(oneArg, newVarsUpdated)
//              (List(argUpdated), newVarsUpdated2)
//            case _ =>
//              args.foldLeft((List.empty[Term], newVarsUpdated)) {
//                case ((argsUpdated, newVarsUpdated), Placeholder()) =>
//                  val (argUpdated, newVarsUpdated2) = replace(Placeholder(), newVarsUpdated)
//                  (argUpdated :: argsUpdated, newVarsUpdated2)
//                case ((argsUpdated, newVarsUpdated), arg) =>
//                  (arg :: argsUpdated, newVarsUpdated)
//              }
//          }
          (
            ApplyInfix(lhsReplaced, op, targs, argsUpdated.reverse),
            newVarsFinal
          )
        case Select(term, name) =>
          val (replacedTerm, newVarsUpdated) = replace(term, newVars, inApplyPart = inApplyPart)
          (Select(replacedTerm, name), newVarsUpdated)
        case Apply(applyTerm, terms) =>
          val (applyTermReplaced, newVarsUpdated) = replace(applyTerm, newVars, inApplyPart = true)
          val (termsUpdated, newVarsFinal) =
            terms.foldLeft((List.empty[Term], newVarsUpdated)) {
              case ((argsUpdated, newVarsUpdated), Placeholder()) =>
                val (argUpdated, newVarsUpdated2) =
                  replace(Placeholder(), newVarsUpdated, inApplyPart = inApplyPart)
                (argUpdated :: argsUpdated, newVarsUpdated2)
              case ((argsUpdated, newVarsUpdated), term) =>
                (term :: argsUpdated, newVarsUpdated)
            }
          (
            Apply(applyTermReplaced, termsUpdated.reverse),
            newVarsFinal
          )
        case ApplyUnary(op, applyTerm) =>
          val (replacedTerm, newVarsUpdated) =
            replace(applyTerm, newVars, inApplyPart = inApplyPart)
          (ApplyUnary(op, replacedTerm), newVarsUpdated)
        case other =>
          (other, newVars)
      }

    replace(initialTerm, initialNewVars, initialInApplyPart)._1
  }

  def countPlaceholders(term: Term, inApplyPart: Boolean): Int =
//    println(("countPlaceholders", term, inApplyPart))
    term match {
      case Placeholder() =>
        1
      case ApplyInfix(lhs, _, _, args) =>
        countPlaceholders(lhs, inApplyPart = false) + {
          if (inApplyPart)
            0
          else {
//            println("@" * 50)
//            println(term)
//            println("@" * 50)
            args match {
              case List(oneArg) => countPlaceholders(oneArg, inApplyPart = false)
              case _            => args.collect { case Placeholder() => 1 }.sum
            }
          }
        }
      case Select(term, _) =>
        countPlaceholders(term, inApplyPart = inApplyPart)
      case Apply(applyTerm, terms) =>
//        println("-" * 30 + " inApplyPart = true " + "-" * 30)
        countPlaceholders(applyTerm, inApplyPart = true) +
          terms.collect { case Placeholder() => 1 }.sum
      case ApplyUnary(_, applyTerm) =>
        countPlaceholders(applyTerm, inApplyPart = inApplyPart)
      case _ =>
        0
    }

  def countFuncPlaceholders(term: Term, inApplyPart: Boolean): Int =
    term match {
      case Function(params, _) =>
        params.count {
          case Param(_, meta.Name.Anonymous(), _, _) => true
          case _                                     => false
        }
      case _ =>
        countPlaceholders(term, inApplyPart)
    }

}
