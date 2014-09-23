import play.Project._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin._

class Builders(org: String, rootScalaVersion: String) {

  Keys.fork in ThisBuild := false

  val cred = {
    val envCredentialsPath = System.getenv("CREDENTIALS_PATH")
    val path = if (envCredentialsPath != null) envCredentialsPath else Seq(Path.userHome / ".ivy2" / ".credentials").mkString
    val f: File = file(path)
    if (f.exists()) {
      println("[credentials] using credentials file")
      Credentials(f)
    } else {
      //https://devcenter.heroku.com/articles/labs-user-env-compile
      def repoVar(s: String) = System.getenv("ARTIFACTORY_" + s)
      val args = Seq("REALM", "HOST", "USER", "PASS").map(repoVar)
      println("[credentials] args: " + args)
      Credentials(args(0), args(1), args(2), args(3))
    }
  }

}