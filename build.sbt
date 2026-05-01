import sbt.Keys.organization

ThisBuild / version := "0.1.1"

ThisBuild / scalaVersion := "2.13.17"

lazy val root = (project in file("."))
  .settings(
    name := "parquetor",
    organization := "com.simple"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "4.1.1",
  "org.apache.spark" %% "spark-sql" % "4.1.1"
)

libraryDependencies ++= Seq(
  "org.jline" % "jline-terminal" % "3.25.1",
  "org.jline" % "jline-reader" % "3.25.1"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
