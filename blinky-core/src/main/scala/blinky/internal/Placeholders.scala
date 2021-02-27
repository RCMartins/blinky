package blinky.internal

import blinky.internal.MutatedTerms.{PlaceholderMutatedTerms, StandardMutatedTerms}

import java.util.UUID
import scala.annotation.tailrec
import scala.meta.{Term, XtensionQuasiquoteTerm}
import scala.meta.Term.{Apply, ApplyInfix, Name, Placeholder, Select}

object Placeholders {

  def replacePlaceholders(
      original: Term,
      mutatedTerms: MutatedTerms
  ): Option[(Term, MutatedTerms)] =
    (original, mutatedTerms) match {
      case (Placeholder(), _) =>
        None
      case (original, mutatedTerms: StandardMutatedTerms)
          if original.collect { case Placeholder() => }.nonEmpty =>
//        println(
//          s"""${"-" * 80}
//             |$original
//             |${original.structure}
//             |${"#" * 0}
//             |${mutatedTerms.mutated.zipWithIndex.map { case (m, i) => s"$i: $m" }.mkString("\n")}
//             |""".stripMargin
//        )
        val isBasicPlaceholderCase =
          original match {
            case ApplyInfix(Placeholder(), _, _, _) =>
              true
            case ApplyInfix(_, _, _, placeholdersList) if containsPlaceholders(placeholdersList) =>
              true
            case ApplyInfix(Select(Apply(_, placeholdersList), _), _, _, _)
                if containsPlaceholders(placeholdersList) =>
              true
            case Apply(Select(Apply(Select(Placeholder(), _), _), _), _) =>
              true
            case _ =>
              true //    false
          }

        if (isBasicPlaceholderCase) {
//          println(mutatedTerms.mutated.map(_.structure))

//          val newVar = Term.Name(s"blinky_${UUID.randomUUID().toString}")
//          println(newVar)
//
//          val placeholderFunction = generatePlaceholderFunction(newVar)
//          val mutatedReplacedOriginal: Seq[(Term, (Term, Boolean))] =
//            mutatedTerms.mutated.map(term => (term, replaceFirstPlaceholder(term, newVar)))
////          val mutatedReplaced = mutatedReplacedOriginal.map { case (a, (b, _)) => (a, b) }
//          val anyMutatedReplaced = mutatedReplacedOriginal.exists { case (_, (_, c)) => c }
//
//          val (originalReplaced, originalB) =
//            replaceFirstPlaceholder(original, newVar)
//
//          println("#" * 50)
//          println(originalReplaced)
//          println(originalB)
//          println(anyMutatedReplaced)
//          println("#" * 50)
//
//          val mutatedReplaced =
//            mutatedReplacedOriginal.map {
//              case (a, (b, true))  => (a, b)
//              case (a, (b, false)) => (defaultPlaceholderFunction(a), b)
//            }

          val (placeholderMode, originalReplaced, mutantsReplaced, vars) =
            replaceAllPlaceholders(original, mutatedTerms.mutated)

          val placeholderFunction = generatePlaceholderFunction(vars)

//          println("%" * 60)
//          println(placeholderMode)
//          println(originalReplaced)
//          println(mutantsReplaced)
//          println(vars)
//          println("%" * 60)

          Some(
            (
              original,
              if (placeholderMode)
                PlaceholderMutatedTerms(
                  originalReplaced,
                  placeholderFunction,
                  mutantsReplaced,
                  vars.map(_.value),
                  mutatedTerms.needsParens
                )
              else
                mutatedTerms
            )
          )
        } else {
          None
        }
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

      val newVar = Term.Name(s"blinky_${UUID.randomUUID().toString}")
//      println(newVar)

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

//      println("#" * 50)
//      println(originalWasReplaced)
//      println(anyMutantsReplaced)
//      println("-" * 5)
//      println(originalReplaced)
//      println(mutatedReplaced)
//      println("#" * 50)

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
      false,
      originalTerm,
      initialMutants.map(term => (term, term)),
      Nil
    )
  }

  private def containsPlaceholders(list: List[Term]): Boolean =
    list.exists {
      case Placeholder() => true
      case _             => false
    }

  private def defaultPlaceholderFunction: Term => Term =
    (body: Term) => q"_ => $body"

  private def generatePlaceholderFunction(newVars: List[Term.Name]): Term => Term =
    (body: Term) =>
      Term.Function(
        newVars.map(newVar => Term.Param(List.empty, newVar, None, None)),
        body
      )

  private def replaceFirstPlaceholder(initialTerm: Term, newVar: Term.Name): (Term, Boolean) = {
    def replaceOnlyP(term: Term): (Term, Boolean) =
      term match {
        case Placeholder() =>
          (newVar, true)
        case _ =>
          (term, false)
      }

    def replace(term: Term): (Term, Boolean) =
//      println(("replace", term.structure))
      term match {
        case Placeholder() =>
          (newVar, true)
        case _: Name =>
          (term, false)
        case ApplyInfix(lhs, op, targs, args) =>
          val (lhsReplaced, varReplaced) = replace(lhs) // replaceOnlyP?
//          println("&" * 20)
//          println(lhs.structure)
//          println((lhsReplaced.structure, varReplaced))
//          println("&" * 20)
          if (varReplaced)
            (ApplyInfix(lhsReplaced, op, targs, args), true)
          else {
            val (argsUpdated, varReplaced) =
              args.foldLeft((List.empty[Term], false)) {
                case ((argsUpdated, false), arg) =>
                  replaceOnlyP(arg) match {
                    case (argUpdated, varReplaced) =>
                      (argUpdated :: argsUpdated, varReplaced)
                  }
                case ((argsUpdated, true), arg) =>
                  (arg :: argsUpdated, true)
              }
//            println("$" * 20)
//            println(args)
//            println(argsUpdated)
//            println("$" * 20)
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
          val (replacedTerm, varReplaced) = replace(applyTerm) // replaceOnlyP?
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
        case other =>
//          println("+" * 20)
//          println(other.structure)
//          println("+" * 20)
          (other, false)
      }

//    Term.Function(
//      List(Term.Param(List.empty, newVar, None, None)),
//    )
    replace(initialTerm)
  }

}
