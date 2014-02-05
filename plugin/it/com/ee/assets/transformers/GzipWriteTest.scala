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

    val gzip = new Gzip()

    val writer = new ByteArrayWriter((p: String) => {
      val f = new File(makePath(outDir, p))
      f.getParentFile.mkdirs()
      f
    }
    )

    "work" in new cleanGenerated(outDir) {
      val zipped = gzip.run(Seq(Element("blah.js", Some(s))))
      val written = writer.run(zipped)
      written.length === 1
      written(0).path
      val readBytes: Array[Byte] = IOUtils.toByteArray(new FileInputStream(new File(written(0).path)))
      decompress(readBytes) === "alert('hello');"
    }

    "work with flow" in new cleanGenerated(outDir) {
      val s = Some("console.log('blah');")
      val combi = gzip.run _ andThen writer.run _
      val out = combi(Seq(Element("blah-two.js", s)))
      val readBytes: Array[Byte] = IOUtils.toByteArray(new FileInputStream(new File(out(0).path)))
      decompress(readBytes) === s.get
    }
  }
}
