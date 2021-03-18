object SBTDefaults {

  lazy val defaultScalacFlags: List[String] =
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

}
