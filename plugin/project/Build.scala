import sbt._
import Keys._
import play.Project._
import sbtrelease.ReleasePlugin._

object Build extends sbt.Build {

  import Dependencies._
  import Resolvers._

  val appName = "assets-loader"
  val ScalaVersion = "2.10.2"


  lazy val integrationTestSettings = Seq(
    scalaSource in IntegrationTest <<= baseDirectory / "it",
    Keys.parallelExecution in IntegrationTest := false,
    Keys.fork in IntegrationTest := false,
    libraryDependencies += specs2 % "it,test",
    libraryDependencies += "com.typesafe.play" %% "play-test" % "2.2.1" % "it,test",
    libraryDependencies += commonsIo % "it,test",
    testOptions in IntegrationTest += Tests.Setup(() => println("Setup Integration Test")),
    testOptions in IntegrationTest += Tests.Cleanup(() => println("Cleanup Integration Test"))
  )

  val main = sbt.Project(appName, file("."))
    .settings(playScalaSettings: _*)
    .settings(releaseSettings: _*)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .settings(integrationTestSettings: _*)
    .settings(
      libraryDependencies ++= Seq(closureCompiler, yuiCompressor, grizzled, commonsIo),
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
              "/home/edeustace/edeustace.com/public/repository" + finalPath))
      }
    )

}
