name := "modicio"
version := "0.1.2.1"
scalaVersion := "2.13.1"


val circeVersion = "0.14.2"

libraryDependencies ++= Seq(
  "io.circe"  %% "circe-core"     % circeVersion,
  "io.circe"  %% "circe-generic"  % circeVersion,
  "io.circe"  %% "circe-parser"   % circeVersion
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-language:implicitConversions",
  "-Xlog-implicits",
  "-deprecation"
)

parallelExecution in Test := false