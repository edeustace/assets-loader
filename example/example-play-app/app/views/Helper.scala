package views

import com.ee.assets.transformers.{PathElement, ExternalHosts}
import play.api.Play
import java.io.InputStream
import com.ee.assets.deployment.{ContentInfo, Deployer}
import com.ee.assets.models.{AssetsInfo, SimpleAssetsInfo}

object Helper{

  val server = "http://some-non-existent-server.com/"
  val deployer : Deployer = new Deployer {
    override def deploy(filename: String, lastModified: Long, contents: => InputStream, info: ContentInfo): Either[String, String] = {

      //do your deployment here...

      println(filename)
      println(lastModified)
      println(info)
      Right(s"$server$filename")
    }
  }


  val externalDeployer: Deployer = new Deployer {
    override def deploy(filename: String, lastModified: Long, contents: => InputStream, info: ContentInfo): Either[String, String] = {
      // do deploy
      Right(filename)
    }

    /*
     * override the method "resolve" is required
     * to hook into process of adding external hosts in rotating order
     */
    /*
    override def resolve(pathElement: PathElement, info: AssetsInfo) = {
      // modify if required
      PathElement(s"$server${pathElement.path}")
    }
    */
  }

  val mode = Play.current.mode
  val config = Play.current.configuration
  val externalInfo = SimpleAssetsInfo("assets", "public", true)

  // just load the scripts
  val loader = new com.ee.assets.Loader(deployer = None, mode = mode, config = config)

  // load with external hosts
  val loaderExternal = new com.ee.assets.Loader(deployer = None, mode = mode, config = config, info = externalInfo)

  // load with deployer
  val deployLoader = new com.ee.assets.Loader(Some(deployer), Play.current.mode, Play.current.configuration)

  // load with deployer and external host
  val externalDeployLoader = new com.ee.assets.Loader(Some(externalDeployer), Play.current.mode, Play.current.configuration, info = externalInfo)
}
