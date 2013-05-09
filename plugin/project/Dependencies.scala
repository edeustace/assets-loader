
import sbt._
import Keys._

object Dependencies{

  //The closure compiler that play uses - we expect this to be provided by the play app.
  val closureCompiler = ("com.google.javascript"            %    "closure-compiler"         %   "rr2079.1" notTransitive())
              .exclude("args4j", "args4j")
              .exclude("com.google.guava", "guava")
              .exclude("org.json", "json")
              .exclude("com.google.protobuf", "protobuf-java")
              .exclude("org.apache.ant", "ant")
              .exclude("com.google.code.findbugs", "jsr305")
              .exclude("com.googlecode.jarjar", "jarjar")
              .exclude("junit", "junit")

  val yuiCompressor = "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7"

  val scalaTools =  "scala-tools" at "http://scala-tools.org/repo-releases/"
  val scalaToolsSnapshots =  "scala-tools-snapshots" at "http://scala-tools.org/repo-snapshots/"
  val commonResolvers = Seq(scalaTools, scalaToolsSnapshots)

  def provided(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
}