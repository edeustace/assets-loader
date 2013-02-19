import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "assets-loader"
    val appVersion      = "0.4-SNAPSHOT"

    val appDependencies = Seq(

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
      resolvers += "scala-tools" at "http://scala-tools.org/repo-releases/",
      resolvers += "scala-tools-snapshots" at "http://scala-tools.org/repo-snapshots/"
    )
}
