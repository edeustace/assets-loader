package com.ee.assets.transformers

import java.io.{StringWriter, StringReader}
import com.ee.log.Logger

class CssMinifier extends Transformer[String,String] {

  lazy val logger = Logger("css-minifier")

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    elements.map {
      e =>
        e.contents.map {
          c =>
            val (name, suffix) = com.ee.utils.file.nameAndSuffix(e.path)
            val nameOut = s"$name.min.$suffix"
            Element(nameOut, Some(minifyCss(c)))
        }.getOrElse {
          logger.warn(s"${e.path} has no contents")
          e
        }
    }
  }


  private def minifyCss(contents: String): String = {
    val compressor = new com.yahoo.platform.yui.compressor.CssCompressor(new StringReader(contents))
    val writer = new StringWriter()
    compressor.compress(writer, 0)
    writer.toString
  }
}
