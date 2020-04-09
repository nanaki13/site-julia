// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x

import java.nio.file.Paths
import scala.sys.process._


enablePlugins(ScalaJSPlugin)
val sharedSettings = Seq(version := "0.1.1-SNAPSHOT",
  organization := "bon.jo",
  scalaVersion := "2.13.1")
name := "html-julia-site"
// or any other Scala version >= 2.11.12

lazy val `julia-shared` =
// select supported platforms
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure) // [Pure, Full, Dummy], default: CrossType.Full
    .settings(sharedSettings)

    .jvmSettings(libraryDependencies += "org.scala-js" %%% "scalajs-stubs" % "1.0.0" % "provided")
// configure Scala-Native settings
// .nativeSettings(/* ... */) // defined in sbt-scala-native

lazy val `julia-jvm` =
// select supported platforms
  crossProject(JVMPlatform)
    .crossType(CrossType.Pure) // [Pure, Full, Dummy], default: CrossType.Full
    .settings(sharedSettings)
    .settings(libraryDependencies ++= Seq(  "org.xerial" % "sqlite-jdbc" % "3.21.0"))

   .dependsOn(`julia-shared`)
lazy val `julia-js` =
// select supported platforms
  crossProject(JSPlatform)
    .crossType(CrossType.Pure) // [Pure, Full, Dummy], default: CrossType.Full
    .settings(sharedSettings)
    .settings(libraryDependencies ++= Seq("org.scala-js" %%% "scalajs-dom" % "1.0.0", "org.scala-lang.modules" %%% "scala-xml" % "2.0.0-M1"
      , "bon.jo" %%% "html-app" % "0.1.0-SNAPSHOT"

    ))

    .settings(
      scalaJSUseMainModuleInitializer := true

    ).dependsOn(`julia-shared`) // defined in sbt-scalajs-crossproject


lazy val chromePath = Paths.get("""C:\Program Files (x86)\Google\Chrome\Application\chrome.exe""")
lazy val chrome = taskKey[Unit]("open fast in chrome")

chrome := {
  s""""$chromePath" "${baseDirectory.value.toPath}/index.html""".!
}

lazy val toServer = taskKey[Unit]("to akka local")

toServer := {


  IO.copyFile(Paths.get("""./julia-js/.js/target/scala-2.13/julia-js-fastopt.js""").toFile, Paths.get("""./html/index.js""").toFile)
  IO.copyFile(Paths.get("""./julia-js/.js/target/scala-2.13/julia-js-fastopt.js.map""").toFile, Paths.get("""./html/julia-js-fastopt.js.map""").toFile)


}
