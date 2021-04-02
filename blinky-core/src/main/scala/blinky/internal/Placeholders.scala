package blinky.internal

import blinky.internal.MutatedTerms.{PlaceholderMutatedTerms, StandardMutatedTerms}

import scala.meta.Term
import scala.meta.Term._

class Placeholders(nextRandomName: () => Name) {

  def replacePlaceholders(
      original: Term,
      placeholderLocation: Option[Term],
      mutatedTerms: MutatedTerms
  ): Option[(Term, MutatedTerms)] =
    (original, mutatedTerms) match {
      case (Placeholder(), _) =>
        None
      case (original, mutatedTerms: StandardMutatedTerms)
          if original.collect { case Placeholder() => }.nonEmpty =>
        val (placeholderMode, originalReplaced, mutantsReplaced, vars) =
          replaceAllPlaceholders(original, mutatedTerms.mutated)

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
      initialMutants: Seq[Term]
  ): (Boolean, Term, Seq[(Term, Term)], List[Name]) = {

    val amountOfPlaceholders: Int = countPlaceholders(originalTerm)

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
//      println(newVars)
//      println(amountOfPlaceholders)
//      println(originalTerm)

      val originalReplaced =
        replaceAllPlaceholders(originalTerm, newVars)

//      println(originalReplaced)
//      println("-" * 50)

      val mutatedReplacedOriginal: Seq[(Term, Term)] =
        initialMutants.map { term =>
          (term, replaceAllPlaceholders(term, newVars))
        }

      val mutatedReplaced =
        mutatedReplacedOriginal.map { case (withP, withoutP) =>
          val amountOfPlaceholdersTerm = countPlaceholders(withP)
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

//      println(initialMutants)
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
    (body: Term) =>
      Term.Function(
        newVars.map(newVar => Term.Param(List.empty, newVar, None, None)),
        body
      )

  private def replaceAllPlaceholders(initialTerm: Term, initialNewVars: List[Name]): Term = {
    def replace(term: Term, newVars: List[Name]): (Term, List[Name]) =
//      println("#" * 20)
//      println(term)
//      println(term.structure)
//      println("#" * 20)
      term match {
        case Placeholder() =>
          (newVars.head, newVars.tail)
        case ApplyInfix(lhs, op, targs, args) =>
          val (lhsReplaced, newVarsUpdated) = replace(lhs, newVars)
          val (argsUpdated, newVarsFinal) =
            args.foldLeft((List.empty[Term], newVarsUpdated)) {
              case ((argsUpdated, newVarsUpdated), arg) =>
                val (argUpdated, newVarsUpdated2) = replace(arg, newVarsUpdated)
                (argUpdated :: argsUpdated, newVarsUpdated2)
            }
          (
            ApplyInfix(lhsReplaced, op, targs, argsUpdated.reverse),
            newVarsFinal
          )
        case Select(term, name) =>
          val (replacedTerm, newVarsUpdated) = replace(term, newVars)
          (Select(replacedTerm, name), newVarsUpdated)
        case Apply(applyTerm, terms) =>
          val (applyTermReplaced, newVarsUpdated) = replace(applyTerm, newVars)
          val (termsUpdated, newVarsFinal) =
            terms.foldLeft((List.empty[Term], newVarsUpdated)) {
              case ((argsUpdated, newVarsUpdated), Placeholder()) =>
                val (argUpdated, newVarsUpdated2) = replace(Placeholder(), newVarsUpdated)
                (argUpdated :: argsUpdated, newVarsUpdated2)
              case ((argsUpdated, newVarsUpdated), term) =>
                (term :: argsUpdated, newVarsUpdated)
            }
          (
            Apply(applyTermReplaced, termsUpdated.reverse),
            newVarsFinal
          )
        case ApplyUnary(op, applyTerm) =>
          val (replacedTerm, newVarsUpdated) = replace(applyTerm, newVars)
          (ApplyUnary(op, replacedTerm), newVarsUpdated)
        case other =>
          (other, newVars)
      }

    replace(initialTerm, initialNewVars)._1
  }

  def countPlaceholders(term: Term): Int =
    term match {
      case Placeholder() =>
        1
      case ApplyInfix(lhs, _, _, args) =>
        countPlaceholders(lhs) + args.map(countPlaceholders).sum
      case Select(term, _) =>
        countPlaceholders(term)
      case Apply(applyTerm, terms) =>
        countPlaceholders(applyTerm) +
          terms.collect { case Placeholder() => 1 }.sum
      case ApplyUnary(_, applyTerm) =>
        countPlaceholders(applyTerm)
      case _ =>
        0
    }

  def countFuncPlaceholders(term: Term): Int =
    term match {
      case Function(params, _) =>
        params.count {
          case Param(_, meta.Name.Anonymous(), _, _) =>
            println(("Name.text", ""))
            true
          case _ => false
        }
      case _ =>
        countPlaceholders(term)
    }

}
