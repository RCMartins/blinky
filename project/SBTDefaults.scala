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
      "-Xlint:deprecation",
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
      "-Xlint:unused",
      "-Ywarn-unused:patvars",
      "-Xlint:adapted-args",
      "-Ywarn-unused:params",
      "-Ywarn-macros:after"
    )

  lazy val scalafixTestkitV: Map[String, String] =
    Map(
      // 2.12.x:
      "2.12.10" -> "0.9.11",
      "2.12.11" -> "0.9.18",
      "2.12.12" -> "0.9.24",
      "2.12.13" -> "0.9.28",
      "2.12.14" -> "0.9.30",
      "2.12.15" -> "0.10.1",
      "2.12.16" -> "0.10.1",
      // 2.13.x:
      "2.13.2" -> "0.9.17",
      "2.13.3" -> "0.9.23",
      "2.13.4" -> "0.9.25",
      "2.13.5" -> "0.9.27",
      "2.13.6" -> "0.9.31",
      "2.13.7" -> "0.9.33",
      "2.13.8" -> "0.10.1"
    )

}
