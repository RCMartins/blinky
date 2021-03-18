object SBTDefaults {

  lazy val defaultScalacFlags212: List[String] =
    List(
      "-encoding",
      "UTF-8",
      "-explaintypes",
      "-unchecked",
      "-feature",
      "-deprecation:true",
      "-Xfuture",
      "-Xcheckinit",
      "-Xlint:by-name-right-associative",
      "-Xlint:constant",
      "-Xlint:delayedinit-select",
      "-Xlint:doc-detached",
      "-Xlint:missing-interpolator",
      "-Xlint:option-implicit",
      "-Xlint:package-object-classes",
      "-Xlint:poly-implicit-overload",
      "-Xlint:private-shadow",
      "-Xlint:stars-align",
      "-Xlint:type-parameter-shadow",
      "-Xlint:unsound-match",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-infer-any",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:privates",
      "-Ypartial-unification",
      "-Yno-adapted-args",
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:params",
      "-Ywarn-macros:after",
      "-Yrangepos",
      "-Xfatal-warnings"
    )

  lazy val defaultScalacFlags213: List[String] =
    List(
      "-encoding",
      "UTF-8",
      "-explaintypes",
      "-unchecked",
      "-feature",
      "-deprecation",
      "-Werror",
      "-Xcheckinit",
      "-Xlint:constant",
      "-Xlint:delayedinit-select",
      "-Xlint:doc-detached",
      "-Xlint:missing-interpolator",
      "-Xlint:option-implicit",
      "-Xlint:package-object-classes",
      "-Xlint:poly-implicit-overload",
      "-Xlint:private-shadow",
      "-Xlint:stars-align",
      "-Xlint:type-parameter-shadow",
      "-Ywarn-dead-code",
      "-Xlint:inaccessible",
      "-Xlint:nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Xlint:infer-any",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:privates",
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:params",
      "-Ywarn-macros:after",
      "-Xlint:adapted-args"
    )

  lazy val scalafixTestkitV: Map[String, String] =
    Map(
      // 2.12.x:
      "2.12.10" -> "0.9.11",
      "2.12.11" -> "0.9.18",
      "2.12.12" -> "0.9.24",
      "2.12.13" -> "0.9.26",
      // 2.13.x:
      "2.13.2" -> "0.9.17",
      "2.13.3" -> "0.9.23",
      "2.13.4" -> "0.9.25",
      "2.13.5" -> "0.9.26"
    )

}
