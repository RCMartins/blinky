---
id: introduction
title: Introduction
sidebar_label: Introduction
---

## What is Mutation Testing?

Mutation testing is used to design new software tests and evaluate the quality of existing software tests.
Mutation testing involves modifying a program in small ways.
Each mutated version is called a mutant and tests detect and reject mutants by causing the behavior of the original version to differ from the mutant. 
This is called killing the mutant. 

Test suites are measured by the percentage of mutants that they kill. 

New tests can be designed to kill additional mutants. 

Mutants are based on well-defined mutation operators that either mimic typical programming errors (such as using the wrong operator or variable name) or force the creation of valuable tests (such as dividing each expression by zero). 

The purpose is to help the tester develop effective tests or locate weaknesses in the test data used for the program or in sections of the code that are seldom or never accessed during execution.

Mutation testing is a form of white-box testing.

## Mutation testing overview

Mutation testing is based on two hypotheses. 
The first is the competent programmer hypothesis. 
This hypothesis states that most software faults introduced by experienced programmers are due to small syntactic errors.
The second hypothesis is called the coupling effect. 
The coupling effect asserts that simple faults can cascade or couple to form other emergent faults.

mutation score = number of mutants killed / total number of mutants

## Definitions

Mutation:

Mutator:

Mutant:

A program that 