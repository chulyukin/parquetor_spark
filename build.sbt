import sbt.Keys.organization

ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.12.20"

lazy val root = (project in file("."))
  .settings(
    name := "parquetor",
    organization := "com.simple"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.6",
  "org.apache.spark" %% "spark-sql" % "3.5.6"
)

libraryDependencies ++= Seq(
  "org.jline" % "jline-terminal" % "3.22.0",
  "org.jline" % "jline-reader" % "3.22.0"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
