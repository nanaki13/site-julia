import Dependencies.scalaTest

name := "site-julia"
lazy val commonSettings = Seq(
  organization := "bon.jo",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.13.1"
)

lazy val wsName = "works-service"
lazy val wrName = "works-repository"
lazy val wr = (project in file(wrName))
  .settings(
    name := wrName,
    commonSettings,
    libraryDependencies ++= Seq( scalaTest % Test,
      "com.typesafe.slick" %% "slick" % "3.3.2",
      "com.h2database" % "h2" % "1.4.192"
      ,"org.postgresql" % "postgresql" %"42.2.5")
  )

lazy val ws = (project in file(wsName)).settings(
  name := wsName,
  commonSettings,
  mainClass in Compile := Some("Main"),
  libraryDependencies ++= Seq(
    scalaTest % Test,
    "com.typesafe.akka" %% "akka-http" % "10.1.11",
    "com.typesafe.akka" %% "akka-stream" % "2.6.1",
    "com.typesafe.akka" %% "akka-slf4j" % "2.6.1",
    "bon.jo"%% "julia-shared"% "0.1.0-SNAPSHOT",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.json4s" %% "json4s-native" % "3.6.7"),
  
  // other settings
).dependsOn(wr)
lazy val root = (project in file(".")).aggregate(wr, ws).settings(
  commonSettings,
  watchSources ++= (baseDirectory.value / "public/ui" ** "*").get
).enablePlugins(JavaAppPackaging)
  //.enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")



