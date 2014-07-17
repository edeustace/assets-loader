package com.ee.assets.transformers

import com.ee.assets.models.AssetsInfo
import com.ee.log.Logger
import play.api.Play
import scala.collection.JavaConverters._
import play.api.Play.current

class FileToWebPath(info: AssetsInfo) extends Transformer[Unit,Unit]{

  lazy val logger = Logger("file-to-web")

  override def run(elements: Seq[Element[Unit]]): Seq[PathElement] = {
    elements.map(e => PathElement(toWebPath(e.path)))
  }

  def toWebPath(p: String): String = {

    if (!p.contains(info.filePath)) {
      logger.warn(s"$p doesn't contain ${info.filePath} - so nothing to replace")
    }
    val out = p.replaceFirst(info.filePath, info.webPath)

    if(info.isExternalHost) ExternalHosts.external(out)
    else if (out.startsWith("/")) out else s"/$out"
  }
}

object ExternalHosts{
  lazy val logger = Logger("external-hosts")

  val configPath = "assetsLoader.hosts"
  var hostsIndex = 0
  lazy val hosts = Play.configuration.getStringList(configPath).fold(List[String]())(_.asScala.toList)

  def external(out: String) = {
    if(ExternalHosts.hosts.size > 0){
      prependNextHost(out)
    }
    else{
      logger.warn(s"no hosts were found from config ${ExternalHosts.configPath}")
      out
    }
  }

  def prependNextHost(out: String) = {
    s"${getNextHost}$out"
  }

  private def getNextHost = {
    val host = ExternalHosts.hosts(ExternalHosts.hostsIndex)
    //iterate through the list of hosts
    if(ExternalHosts.hostsIndex == ExternalHosts.hosts.size-1)  ExternalHosts.hostsIndex = 0
    else ExternalHosts.hostsIndex += 1

    host
  }

}