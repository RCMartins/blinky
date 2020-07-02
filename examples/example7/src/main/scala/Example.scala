object Example {

  sealed trait ValueType

  case object Number extends ValueType

  case object String extends ValueType

  def show(values: List[ValueType]): String =
    values.map {
      case Number => "<number>"
      case String => "<string>"
    }.mkString("[", ", ", "]")
}
