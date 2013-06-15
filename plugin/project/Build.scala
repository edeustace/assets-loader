import sbt._
import Keys._
import play.Project._

object Build extends sbt.Build {

    import Dependencies._

    val appName         = "assets-loader"
    val appVersion      = "0.10.1-SNAPSHOT"
    val ScalaVersion = "2.10.1"

    //TODO: Use the closure compiler from play sbt plugin
    val main = play.Project(appName, appVersion, Seq(closureCompiler, yuiCompressor)).settings(
      resolvers ++= commonResolvers,
      organization := "com.ee",
      scalaVersion := ScalaVersion,
      publishMavenStyle := true,
      publishTo <<= version { (v: String) =>
        def isSnapshot = v.trim.endsWith("SNAPSHOT")
        val finalPath = (if (isSnapshot) "/snapshots" else "/releases")
        Some(
          Resolver.sftp(
            "Ed Eustace",
            "edeustace.com",
            "/home/edeustace/edeustace.com/public/repository" + finalPath ))
       }
       )

}
