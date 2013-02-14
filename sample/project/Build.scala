import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "assets-loader-sample"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "assets-loader" %% "assets-loader" % "0.1-SNAPSHOT",
     ("com.google.javascript"            %    "closure-compiler"         %   "rr2079.1" notTransitive())
              .exclude("args4j", "args4j")
              .exclude("com.google.guava", "guava")
              .exclude("org.json", "json")
              .exclude("com.google.protobuf", "protobuf-java")
              .exclude("org.apache.ant", "ant")
              .exclude("com.google.code.findbugs", "jsr305")
              .exclude("com.googlecode.jarjar", "jarjar")
              .exclude("junit", "junit")
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here
    resolvers += "Local Play Repository" at "file:///Users/edeustace/dev/frameworks/play-2.0.4/repository/local"
  )

}
