package test.general

object GeneralSyntaxFor {
  for {
    case (a, b) <- List((1, 2), (if (???) 1 - 2 else 1 + 2, 4))
    c = if (???) a - b else a + b
    x <- 1 to (if (???) 50 - 50 else 50 + 50)
    if (if (???) x % 7 < 3 else !(x % 7 < 3))
  } println(x)

  for {
    case (a, b) <- List((1, 2), (if (???) 1 - 2 else 1 + 2, 4))
    c = if (???) a - b else a + b
    x <- 1 to (if (???) 50 - 50 else 50 + 50)
    if (if (???) x % 7 < 3 else !(x % 7 < 3))
  } yield x
}
