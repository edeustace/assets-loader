package com.ee.assets.transformers

import com.ee.assets.exceptions.AssetsLoaderException
import com.ee.log.Logger
import java.io.File
import java.net.JarURLConnection
import org.apache.commons.io.IOUtils
import play.api.Play

class PlayResourceReader extends Transformer[Unit, String] {

  lazy val logger = Logger("play-resource-reader")

  import play.api.Play.current

  override def run(elements: Seq[Element[Unit]]): Seq[Element[String]] = {

    elements.map {
      e =>
        import scala.io.Source

        val url = Play.resource(e.path)

        val contents = url.map {
          u =>
            IOUtils.toString(u.openStream())
        }.orElse {
          val errorMsg = s"can't load path: ${e.path}"
          throw new AssetsLoaderException(errorMsg)
        }

        val lastModified: Option[Long] = url.map(maybeLastModified).flatten

        contents.map{ c =>
          ContentElement[String](path = e.path, contents = c, lastModified = lastModified)
        }
    }.flatten
  }

  def maybeLastModified(resource: java.net.URL): Option[Long] = {
    resource.getProtocol match {
      case "file" => Some(new File(resource.getPath).lastModified)
      case "jar" => {
        Option(resource.openConnection)
          .map(_.asInstanceOf[JarURLConnection].getJarEntry.getTime)
          .filterNot(_ == -1)
      }
      case _ => None
    }
  }

}
