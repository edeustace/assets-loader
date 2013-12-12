import sbt._
import Keys._

object Build extends sbt.Build {

  val appName         = "example-2_2_1"
  val appVersion      = "1.0"

  val assetsLoader = "com.ee" %% "assets-loader" % "0.10.2-4ad4a9e"

  val appDependencies = Seq(assetsLoader)

  val edeustaceReleases= "ed eustace" at "http://edeustace.com/repository/releases/"
  val edeustaceSnapshots = "ed eustace snapshots" at "http://edeustace.com/repository/snapshots/"

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(edeustaceReleases,edeustaceSnapshots),
    organization := "com.ee"
  )

}
