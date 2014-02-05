package com.ee.assets.transformers.helpers

import java.io.{InputStreamReader, BufferedReader, ByteArrayInputStream}
import java.util.zip.GZIPInputStream

trait GzipHelper {

  def decompress(bytes:Array[Byte]) = {
    val gis: GZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))
    val bf: BufferedReader = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"))
    var outString: String = ""
    def run: Unit = {
      val l = bf.readLine()
      if (l != null) {
        outString += l
        run
      }
    }
    run
    outString
  }
}
