package com.ee.assets.transformers

import com.ee.assets.exceptions.AssetsLoaderException
import play.api.Play
import com.ee.log.Logger

class PlayResourceReader extends Transformer[String,String] {

  lazy val logger = Logger("play-resource-reader")

  import play.api.Play.current

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    elements.map {
      e =>
        if (e.contents.isDefined) {
          throw new AssetsLoaderException(s"${e.path} already has contents")
        }
        import scala.io.Source

        if (Play.resource(e.path).isEmpty) {
          logger.warn(s"can't load path: ${e.path}")
        }
        val contents = Play.resourceAsStream(e.path).map {
          Source.fromInputStream(_).getLines.mkString("\n")
        }
        e.copy(contents = contents)
    }
  }
}
