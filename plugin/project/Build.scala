import sbt._
import Keys._
import play.Project._
import sbtrelease.ReleasePlugin._

object Build extends sbt.Build {

    import Dependencies._

    val appName      = "assets-loader"
    val ScalaVersion = "2.10.1"

    val main = sbt.Project(appName, file("."))
      .settings(playScalaSettings: _*)
      //, appVersion, Seq(closureCompiler, yuiCompressor))
      .settings(releaseSettings: _*)
      .settings(
        libraryDependencies ++= Seq(closureCompiler, yuiCompressor),
        resolvers ++= commonResolvers,
        organization := "com.ee",
        scalaVersion := ScalaVersion,
        publishMavenStyle := true,
        publishTo <<= version {
          (v: String) =>
            def isSnapshot = v.trim.contains("-")
            val finalPath = (if (isSnapshot) "/snapshots" else "/releases")
        Some(
          Resolver.sftp(
            "Ed Eustace",
            "edeustace.com",
            "/home/edeustace/edeustace.com/public/repository" + finalPath ))
       }
       )

}
