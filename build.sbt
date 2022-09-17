name := "modicio"
version := "0.1"
scalaVersion := "2.13.8"

val circeVersion = "0.14.2"

libraryDependencies ++= Seq(
  "io.circe"  %% "circe-core"     % circeVersion,
  "io.circe"  %% "circe-generic"  % circeVersion,
  "io.circe"  %% "circe-parser"   % circeVersion
)

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-language:implicitConversions",
  "-Xlog-implicits",
  "-deprecation"
)