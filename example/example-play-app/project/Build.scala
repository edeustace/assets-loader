import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "example-212"
  val appVersion      = "1.0-SNAPSHOT"

  val assetsLoader = "com.ee" %% "assets-loader" % "0.10-SNAPSHOT"

  val appDependencies = Seq(assetsLoader)

  val edeustaceReleases= "ed eustace" at "http://edeustace.com/repository/releases/"
  val edeustaceSnapshots = "ed eustace snapshots" at "http://edeustace.com/repository/snapshots/"

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(edeustaceReleases,edeustaceSnapshots)
  )

}
