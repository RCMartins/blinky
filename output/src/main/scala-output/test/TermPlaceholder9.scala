package test

object TermPlaceholder9 {

  def func: (String, String) => String = (_1_, _2_) => ///
         if (???) "mutated!" ///
    else if (???) "" ///
             else _1_ + _2_

}
