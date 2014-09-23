import sbt._
import Keys._
import play.Project._
import sbtrelease.ReleasePlugin._

object Build extends sbt.Build {

  import Dependencies._
  import Resolvers._

  val appName = "assets-loader"
  val org = "org.corespring"
  val ScalaVersion = "2.10.2"

  val builder = new Builders(org, ScalaVersion)

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
      credentials += builder.cred,
      libraryDependencies ++= Seq(closureCompiler, yuiCompressor, grizzled, commonsIo),
      resolvers ++= commonResolvers,
      organization := "org.corespring",
      scalaVersion := ScalaVersion,
      publishMavenStyle := true,
      publishTo <<= version {
        (v: String) =>
          def isSnapshot = v.trim.contains("-")
          val base = "http://repository.corespring.org/artifactory"
          val repoType = if (isSnapshot) "snapshot" else "release"
          val finalPath = base + "/ivy-" + repoType + "s"
          Some("Artifactory Realm" at finalPath)
      }
    )

}
