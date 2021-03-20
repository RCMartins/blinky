package blinky.internal

import blinky.internal.MutatedTerms.{PlaceholderMutatedTerms, StandardMutatedTerms}

import java.util.UUID
import scala.annotation.tailrec
import scala.meta.{Term, XtensionQuasiquoteTerm}
import scala.meta.Term.{Apply, ApplyInfix, ApplyUnary, Name, Placeholder, Select}

object Placeholders {

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

  def replaceAllPlaceholders(
      originalTerm: Term,
      initialMutants: Seq[Term]
  ): (Boolean, Term, Seq[(Term, Term)], List[Name]) = {

    @tailrec
    def loop(
        placeholderMode: Boolean,
        originalTerm: Term,
        finalMutants: Seq[(Term, Term)],
        vars: List[Name]
    ): (Boolean, Term, Seq[(Term, Term)], List[Name]) = {

      val newVar = Name(s"blinky_${UUID.randomUUID().toString}")

      val mutatedReplacedOriginal: Seq[(Term, (Term, Boolean))] =
        finalMutants.map { case (termO, termP) =>
          (termO, replaceFirstPlaceholder(termP, newVar))
        }
      val anyMutantsReplaced =
        mutatedReplacedOriginal.exists { case (_, (_, mutantReplaced)) => mutantReplaced }
      val (originalReplaced, originalWasReplaced) =
        replaceFirstPlaceholder(originalTerm, newVar)

      val mutatedReplaced =
        mutatedReplacedOriginal.map {
          case (withP, (withoutP, true))  => (withP, withoutP)
          case (withP, (withoutP, false)) => (defaultPlaceholderFunction(withP), withoutP)
        }

      if (anyMutantsReplaced)
        loop(
          placeholderMode = true,
          originalReplaced,
          mutatedReplaced,
          newVar :: vars
        )
      else if (originalWasReplaced)
        (
          true,
          originalReplaced,
          mutatedReplaced,
          newVar :: vars
        )
      else
        (
          placeholderMode,
          originalTerm,
          finalMutants,
          vars.reverse
        )
    }

    loop(
      placeholderMode = false,
      originalTerm,
      initialMutants.map(term => (term, term)),
      Nil
    )
  }

  private def defaultPlaceholderFunction: Term => Term =
    (body: Term) => q"_ => $body"

  private def generatePlaceholderFunction(newVars: List[Name]): Term => Term =
    (body: Term) =>
      Term.Function(
        newVars.map(newVar => Term.Param(List.empty, newVar, None, None)),
        body
      )

  private def replaceFirstPlaceholder(initialTerm: Term, newVar: Name): (Term, Boolean) = {
    def replaceOnlyP(term: Term): (Term, Boolean) =
      term match {
        case Placeholder() =>
          (newVar, true)
        case _ =>
          (term, false)
      }

    def replace(term: Term): (Term, Boolean) =
      term match {
        case Placeholder() =>
          (newVar, true)
        case ApplyInfix(lhs, op, targs, args) =>
          val (lhsReplaced, varReplaced) = replace(lhs)
          if (varReplaced)
            (ApplyInfix(lhsReplaced, op, targs, args), true)
          else {
            val (argsUpdated, varReplaced) =
              args.foldLeft((List.empty[Term], false)) {
                case ((argsUpdated, false), arg) =>
                  replace(arg) match {
                    case (argUpdated, varReplaced) =>
                      (argUpdated :: argsUpdated, varReplaced)
                  }
                case ((argsUpdated, true), arg) =>
                  (arg :: argsUpdated, true)
              }
            (
              ApplyInfix(
                lhs,
                op,
                targs,
                argsUpdated.reverse
              ),
              varReplaced
            )
          }
        case Select(term, name) =>
          val (replacedTerm, varReplaced) = replace(term)
          (Select(replacedTerm, name), varReplaced)
        case Apply(applyTerm, terms) =>
          val (replacedTerm, varReplaced) = replace(applyTerm)
          if (varReplaced)
            (Apply(replacedTerm, terms), varReplaced)
          else {
            val (termsUpdated, varReplaced) =
              terms.foldLeft((List.empty[Term], false)) {
                case ((termsUpdated, false), term) =>
                  replaceOnlyP(term) match {
                    case (termUpdated, varReplaced) =>
                      (termUpdated :: termsUpdated, varReplaced)
                  }
                case ((termsUpdated, true), term) =>
                  (term :: termsUpdated, true)
              }
            (Apply(applyTerm, termsUpdated.reverse), varReplaced)
          }
        case ApplyUnary(op, applyTerm) =>
          val (replacedTerm, varReplaced) = replace(applyTerm)
          (ApplyUnary(op, replacedTerm), varReplaced)
        case other =>
          (other, false)
      }

    replace(initialTerm)
  }

}
