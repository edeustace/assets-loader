package com.ee.assets.transformers

import com.ee.assets.transformers.helpers.GzipHelper
import java.io.{FileInputStream, File}
import org.apache.commons.io.IOUtils
import org.specs2.mutable.Specification


class GzipWriteTest
  extends Specification
  with BaseIntegration
  with GzipHelper {

  "gzip -> write" should {

    val s = "alert('hello');"
    val outDir = makePath("target", "test-files", "gzip-files")

    val gzip = Gzip()

    val write = ByteArrayWriter( resolveFileFn(outDir))

    "work" in new cleanGenerated(outDir) {
      val zipped = gzip(Seq(ContentElement("blah.js", s, None)))
      val written = write(zipped)
      written.length === 1
      written(0).path
      val readBytes: Array[Byte] = IOUtils.toByteArray(new FileInputStream(new File( s"$outDir${File.separator}${written(0).path}")))
      decompress(readBytes) === "alert('hello');"
    }

    "work with flow" in new cleanGenerated(outDir) {
      val s = "console.log('blah');"
      val combi = gzip andThen write
      val out = combi(Seq(ContentElement("blah-two.js", s, None)))
      val readBytes: Array[Byte] = IOUtils.toByteArray(new FileInputStream(new File( s"$outDir${File.separator}${out(0).path}")))
      decompress(readBytes) === s
    }
  }
}
