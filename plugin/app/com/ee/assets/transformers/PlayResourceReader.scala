package com.ee.assets.transformers

import com.ee.assets.exceptions.AssetsLoaderException
import com.ee.log.Logger
import java.io.File
import java.net.JarURLConnection
import play.api.Play

class PlayResourceReader extends Transformer[String, String] {

  lazy val logger = Logger("play-resource-reader")

  import play.api.Play.current

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    elements.map {
      e =>
        if (e.contents.isDefined) {
          throw new AssetsLoaderException(s"${e.path} already has contents")
        }
        import scala.io.Source

        val url = Play.resource(e.path)

        val contents = url.map {
          u =>
            Source.fromInputStream(u.openStream).getLines.mkString("\n")
        }.orElse {
          val errorMsg = s"can't load path: ${e.path}"
          throw new AssetsLoaderException(errorMsg)
        }

        val lastModified: Option[Long] = url.map(maybeLastModified).flatten

        e.copy(contents = contents, lastModified = lastModified)
    }
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
