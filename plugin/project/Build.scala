import sbt._
import Keys._
import PlayProject._

object Build extends sbt.Build {

    import Dependencies._

    val appName         = "assets-loader"
    val appVersion      = "0.8-SNAPSHOT"
    val ScalaVersion = "2.9.1"

    val main = PlayProject(appName, appVersion, provided(closureCompiler), mainLang = SCALA).settings(
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
