package com.ee.assets.transformers

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class Gzip extends Transformer[String,Array[Byte]] {
  override def run(elements: Seq[Element[String]]): Seq[Element[Array[Byte]]] = {

    elements.map {
      e: Element[String] =>
        val (name, suffix) = com.ee.utils.file.nameAndSuffix(e.path)
        val dir = grizzled.file.util.dirname(e.path)
        val nameOut = if(dir == "." ) s"$name.gz.$suffix" else s"$dir/$name.gz.$suffix"
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
