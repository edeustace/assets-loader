
import sbt._
import Keys._

object Dependencies{

  //The closure compiler that play uses - we expect this to be provided by the play app.
  val closureCompiler = ("com.google.javascript" % "closure-compiler" % "rr2079.1" )
              .exclude("args4j", "args4j")
              .exclude("org.json", "json")
              .exclude("com.google.protobuf", "protobuf-java")
              .exclude("org.apache.ant", "ant")
              .exclude("com.google.code.findbugs", "jsr305")
              .exclude("com.googlecode.jarjar", "jarjar")
              .exclude("junit", "junit")

  val yuiCompressor = "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7"

  val grizzled = "org.clapper" % "grizzled-scala_2.10" % "1.1.4"

  val specs2 = "org.specs2" %% "specs2" % "2.3.7"
  val commonsIo = "commons-io" % "commons-io" % "2.4"

  def provided(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
}

object Resolvers {

  val scalaTools =  "scala-tools" at "http://scala-tools.org/repo-releases/"
  val scalaToolsSnapshots =  "scala-tools-snapshots" at "http://scala-tools.org/repo-snapshots/"
  val commonResolvers = Seq(scalaTools, scalaToolsSnapshots)

}