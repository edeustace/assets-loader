import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "example-221"
  val appVersion      = "1.0-SNAPSHOT"

  val assetsLoader = "com.ee" %% "assets-loader" % "0.12.2-SNAPSHOT"

  val appDependencies = Seq(assetsLoader)

  //Publish the build locally first
  val edeustaceReleases= "ed eustace" at "http://edeustace.com/repository/releases/"
  val edeustaceSnapshots = "ed eustace snapshots" at "http://edeustace.com/repository/snapshots/"

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq.empty)
  //edeustaceReleases,edeustaceSnapshots)
  

}
