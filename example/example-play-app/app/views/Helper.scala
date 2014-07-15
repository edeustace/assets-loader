package views

import com.ee.assets.transformers.ExternalHosts
import play.api.Play
import java.io.InputStream
import com.ee.assets.deployment.{ContentInfo, Deployer}
import com.ee.assets.models.SimpleAssetsInfo

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

  val externalDeployer: Deployer = new Deployer {
    override def deploy(filename: String, lastModified: Long, contents: => InputStream, info: ContentInfo): Either[String, String] = {

      //do your deployment here and get a host from the config...

      println(filename)
      println(lastModified)
      println(info)
      val host = s"${ExternalHosts.getNextHost}$filename"
      Right(host)
    }
  }

  val deploy = None
  val mode = Play.current.mode
  val config = Play.current.configuration

  val loader = new com.ee.assets.Loader(deployer = deploy, mode = mode, config = config, info = SimpleAssetsInfo("assets", "public"))
  val loaderExternal = new com.ee.assets.Loader(deployer = deploy, mode = mode, config = config, info = SimpleAssetsInfo("assets", "public", true))

  val deployLoader = new com.ee.assets.Loader(Some(deployer), Play.current.mode, Play.current.configuration)
  val externalDeployLoader = new com.ee.assets.Loader(Some(externalDeployer), Play.current.mode, Play.current.configuration)
}
