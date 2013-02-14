import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "assets-loader"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "scala-tools" at "http://scala-tools.org/repo-releases/",
      resolvers += "scala-tools-snapshots" at "http://scala-tools.org/repo-snapshots/"
    )
}
