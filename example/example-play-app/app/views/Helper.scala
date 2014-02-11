package views

import play.api.Play
import java.io.InputStream
import com.ee.assets.deployment.{ContentInfo, Deployer}

object Helper{

  val deployer : Deployer = new Deployer {
    override def deploy(filename: String, lastModified: Long, contents: => InputStream, info: ContentInfo): Either[String, String] = {

      //do your deployment here...

      println(filename)
      println(lastModified)
      println(info)
      Right(s"http://some-non-existent-server.com/$filename")
    }
  }

  val loader = new com.ee.assets.Loader(None, Play.current.mode, Play.current.configuration)
  val deployLoader = new com.ee.assets.Loader(Some(deployer), Play.current.mode, Play.current.configuration)
}