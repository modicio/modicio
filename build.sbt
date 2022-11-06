name := "modicio"
version := "0.1.1"
scalaVersion := "2.12.17"

val circeVersion = "0.14.2"

libraryDependencies ++= Seq(
  "io.circe"  %% "circe-core"     % circeVersion,
  "io.circe"  %% "circe-generic"  % circeVersion,
  "io.circe"  %% "circe-parser"   % circeVersion
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"
libraryDependencies += "org.scoverage" % "sbt-scoverage_2.12_1.0" % "2.0.6"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-language:implicitConversions",
  "-Xlog-implicits",
  "-deprecation"
)