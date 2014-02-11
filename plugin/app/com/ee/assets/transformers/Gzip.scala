package com.ee.assets.transformers

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import com.ee.log.Logger

object Gzip {
  def apply() = new Gzip().run _
}

class Gzip extends Transformer[String, Array[Byte]] {

  val logger = Logger("gzip")

  override def run(elements: Seq[Element[String]]): Seq[Element[Array[Byte]]] = {

    elements.map {
      e: Element[String] =>
        val (name, suffix) = com.ee.utils.file.nameAndSuffix(e.path)
        logger.trace(s"name: $name, suffix: $suffix")
        val nameOut = s"$name.gz.$suffix"
        logger.trace(s"name out: $nameOut")
        val out: Element[Array[Byte]] = ContentElement[Array[Byte]](nameOut, gzipIt(e.contents), e.lastModified)
        out
    }
  }

  private def gzipIt(s: String): Array[Byte] = {
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    val gzip: GZIPOutputStream = new GZIPOutputStream(out)
    gzip.write(s.getBytes())
    gzip.close()
    out.toByteArray
  }
}
