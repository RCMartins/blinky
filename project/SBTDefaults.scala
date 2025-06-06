object SBTDefaults {

  lazy val defaultScalacFlags212: List[String] =
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

  lazy val defaultScalacFlags213: List[String] =
    List(
      "-deprecation",
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

  // check latest https://mvnrepository.com/artifact/ch.epfl.scala/scalafix-testkit
  lazy val scalafixTestkitV: Map[String, String] =
    Map(
      // 2.12.x:
      "2.12.10" -> "0.9.11",
      "2.12.11" -> "0.9.18",
      "2.12.12" -> "0.9.24",
      "2.12.13" -> "0.9.26",
      "2.12.14" -> "0.9.30",
      "2.12.15" -> "0.9.34",
      "2.12.16" -> "0.10.1",
      "2.12.17" -> "0.10.4",
      "2.12.18" -> "0.11.0",
      "2.12.19" -> "0.12.0",
      "2.12.20" -> "0.14.3",
      // 2.13.x:
      "2.13.2" -> "0.9.17",
      "2.13.3" -> "0.9.23",
      "2.13.4" -> "0.9.25",
      "2.13.5" -> "0.9.26",
      "2.13.6" -> "0.9.31",
      "2.13.7" -> "0.9.33",
      "2.13.8" -> "0.9.34",
      "2.13.9" -> "0.10.3",
      "2.13.10" -> "0.10.4",
      "2.13.11" -> "0.11.0",
      "2.13.12" -> "0.11.1",
      "2.13.13" -> "0.12.0",
      "2.13.14" -> "0.12.1",
      "2.13.15" -> "0.13.0",
      "2.13.16" -> "0.14.3",
    )

}
