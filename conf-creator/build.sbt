
ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "bon.jo"
ThisBuild / organizationName := "bon.joo"

lazy val wr = (project in file("."))
  .settings(
    name := "conf-creator",
    libraryDependencies ++= Seq(
    "com.h2database" % "h2" % "1.4.192"),
      libraryDependencies += "org.scala-lang" % "scala-reflect"  % scalaVersion.value
)

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
