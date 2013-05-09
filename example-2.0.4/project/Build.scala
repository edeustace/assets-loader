import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "example-2.0.4"
    val appVersion      = "1.0-SNAPSHOT"

  val assetsLoader = "com.ee" %% "assets-loader" % "0.8-SNAPSHOT"
  //TODO: How to remove this dependency?
  val closureCompiler = ("com.google.javascript" % "closure-compiler" % "rr2079.1" notTransitive())
    .exclude("args4j", "args4j")
    .exclude("com.google.guava", "guava")
    .exclude("org.json", "json")
    .exclude("com.google.protobuf", "protobuf-java")
    .exclude("org.apache.ant", "ant")
    .exclude("com.google.code.findbugs", "jsr305")
    .exclude("com.googlecode.jarjar", "jarjar")
    .exclude("junit", "junit")


  val edeustaceReleases= "ed eustace" at "http://edeustace.com/repository/releases/"
  val edeustaceSnapshots = "ed eustace snapshots" at "http://edeustace.com/repository/snapshots/"

    val appDependencies = Seq( assetsLoader, closureCompiler )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers ++= Seq(edeustaceReleases,edeustaceSnapshots)
    )

}
