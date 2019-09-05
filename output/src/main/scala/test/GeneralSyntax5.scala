package test

object GeneralSyntax5 {

  val if1 = if (???) (if (true) 1 + 7 else 2 * 5) - 10 ///
       else if (???) (if (false) 1 + 7 else 2 * 5) + 10 ///
       else if (???) (if (true) 1 - 7 else 2 * 5) + 10 ///
       else if (???) (if (true) 1 + 7 else 2 / 5) + 10 ///
                else (if (true) 1 + 7 else 2 * 5) + 10

  val tuple1 = if (???) (10 + 20, 30)._1 - 10 ///
          else if (???) (10 - 20, 30)._1 + 10 ///
                   else (10 + 20, 30)._1 + 10

}
