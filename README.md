[![Build Status](https://travis-ci.com/RCMartins/blinky.svg?branch=master)](https://travis-ci.com/RCMartins/blinky)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9bc5c989d1464a6ca94da021ee43d8f6)](https://www.codacy.com/manual/RCMartins/blinky?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RCMartins/blinky&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9bc5c989d1464a6ca94da021ee43d8f6)](https://www.codacy.com/manual/RCMartins/blinky?utm_source=github.com&utm_medium=referral&utm_content=RCMartins/blinky&utm_campaign=Badge_Coverage)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.rcmartins/MutateCode_2.12.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.rcmartins%22%20AND%20a:%22MutateCode_2.12%22)



Mutation Testing is a type of software testing where we mutate (change) certain expressions in the source code 
and check if the test cases are able to find the errors.
It is a type of White Box Testing which is mainly used for Unit Testing.

This is implemented using 




# Mutators

Mutators are the transformations to the code that we want to apply.
Because of several factors like time to run or importance we may want to enable/disable some
of the available mutators.

# Available Mutators

## Literal Booleans
#### name: LiteralBooleans
#### description: change true into false and false into true.  
#### example:

`val bool = true`

after mutator:

`val bool = false`

## Arithmetic Operators

### group name: ArithmeticOperators

### Int - Plus into Minus
#### name: IntPlusToMinus
#### description: Changes the arithmetic operator `+` into `-` when operating on `int` types.
#### example:

`val value = list.size + 5`

after mutator:

`val value = list.size - 5`

(Note that it only applies to `int` types)

### Int - Minus into Plus
#### name: IntMinusToPlus
#### description: Changes the arithmetic operator `-` into `+` when operating on `int` types.

### Int - Multiply into Divide
#### name: IntMulToDiv
#### description: Changes the arithmetic operator `*` into `/` when operating on `int` types.

### Int - Divide into Multiply
#### name: IntDivToMul
#### description: Changes the arithmetic operator `/` into `*` when operating on `int` types.

## Conditional Expressions

### group name: ConditionalExpressions

### Boolean - And into Or
#### name: AndToOr
#### description: Changes the conditional operator `&&` to `||` on `boolean` types.

### Boolean - Or into And
#### name: OrToAnd
#### description: Changes the conditional operator `||` to `&&` on `boolean` types.

### Boolean - Remove negation
#### name: RemoveUnaryNot
#### description: Removes the `!` operator on `boolean` types.

example:

`if (!value) 10 else 20`

after mutation:

`if (value) 10 else 20`

## Literal Strings

### group name: LiteralStrings

### String - Change empty string
#### name: EmptyToMutated
#### description: Changes empty strings `""` into `"mutated!"`.

example:

`val str = ""` 

after mutation:

`val str = "mutated!"`

### String - Change non-empty string
#### name: NonEmptyToMutated
#### description: Changes non-empty strings into empty `""` and also into `"mutated!"`

example:

`val name = "foobar"`

after mutation 1:

`val name = ""`

after mutation 2:

`val name = "mutated!"`

### String - Concat into mutated
#### name: ConcatToMutated
#### description: Changes the string concat operator `+` and both left and right expressions into empty `""` and also into `"mutated!"`

example:

`val name = "foo" + "bar"`

after mutation 1:

`val name = ""`

after mutation 2:

`val name = "mutated!"`

## Scala Options

### group name: ScalaOptions

### GetOrElse

#### name: GetOrElse
#### description: Changes the scala.Option `getOrElse` function call into the default value.

#### example:

```diff
  val option: Option[Int]
- val value = option.getOrElse(10)
+ val value = 10
```

#### example:

```
val option: Option[Int]
val value = option.getOrElse(10)
```

after mutation:

```
val option: Option[Int]
val value = 10
```

### Exists

#### name: Exists
#### description: Changes the scala.Option `exists` into `forall`

### Forall

#### name: Forall
#### description: Changes the scala.Option `forall` into `exists`

### IsEmpty

#### name: IsEmpty
#### description: Changes the scala.Option `isEmpty` into `nonEmpty`

### NonEmpty

#### name: NonEmpty
#### description: Changes the scala.Option `nonEmpty` and `isDefined` into `isEmpty`

### Fold

#### name: Fold
#### description: Changes the scala.Option `fold` function call into the default value.

### OrElse

#### name: OrElse
#### description: 

### OrNull

#### name: OrNull
#### description: 

### Filter

#### name: Filter
#### description: 

### FilterNot

#### name: FilterNot
#### description: 

### Contains

#### name: Contains
#### description: 

