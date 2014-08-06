package views

import play.api.{Logger, Play}
import java.io.InputStream
import com.ee.assets.deployment.{ContentInfo, Deployer}
import com.ee.assets.models.SimpleAssetsInfo
import com.ee.assets.transformers.{SimpleDeployedElement, DeployedElement}

object Helper{

  val deployer : Deployer = new Deployer {
    override def deploy(filename: String, lastModified: Long, contents: => InputStream, info: ContentInfo): Either[String, DeployedElement] = {

      //do your deployment here...

      println(filename)
      println(lastModified)
      println(info)
      Right(SimpleDeployedElement(s"http://some-non-existent-server.com/$filename"))
    }
  }

  object ExternalHosts{

    import Play.current

    lazy val logger = Logger("external-hosts")

    val configPath = "assets.hosts"
    var hostsIndex = 0
    import scala.collection.JavaConverters._

    lazy val hosts = Play.configuration.getStringList(configPath).fold(List[String]())(_.asScala.toList)

    def getNextHost = {
      val host = ExternalHosts.hosts(ExternalHosts.hostsIndex)
      if(ExternalHosts.hostsIndex == ExternalHosts.hosts.size-1)  ExternalHosts.hostsIndex = 0
      else ExternalHosts.hostsIndex += 1
      host
    }
  }

  case class RotatingHostsDeployedElement(deployedPath: String, getHost: () => String) extends DeployedElement{
    override def path: String = s"${getHost()}$deployedPath"
  }

  /**
   * An example of rotating hosts for a deployed element.
   */
  val rotatingDeployer : Deployer = new Deployer {
    override def deploy(filename: String, lastModified: Long, contents: => InputStream, info: ContentInfo): Either[String, DeployedElement] = {

      //do your deployment here...

      println(filename)
      println(lastModified)
      println(info)
      Right(RotatingHostsDeployedElement(filename, ExternalHosts.getNextHost _))
    }
  }

  val loader = new com.ee.assets.Loader(None, Play.current.mode, Play.current.configuration, info = SimpleAssetsInfo("assets", "public"))
  val deployLoader = new com.ee.assets.Loader(Some(deployer), Play.current.mode, Play.current.configuration)
  val rotatingHostsLoader = new com.ee.assets.Loader(Some(rotatingDeployer), Play.current.mode, Play.current.configuration)
}
