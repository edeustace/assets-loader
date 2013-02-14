import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "assets-loader-sample"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "assets-loader" %% "assets-loader" % "0.1-SNAPSHOT"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here
    resolvers += "Local Play Repository" at "file:///Users/edeustace/dev/frameworks/play-2.0.4/repository/local"
  )

}
