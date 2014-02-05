package com.ee.assets.transformers

import com.ee.assets.transformers.helpers.GzipHelper
import java.io._
import org.apache.commons.io.IOUtils
import org.specs2.mutable.Specification
import scala.Some

class GzipTest extends Specification with GzipHelper {

  "GZip" should {

    "work" in {
      val elements = Seq(Element("/path.js", Some("alert('hello');")))
      val zipped = new Gzip().run(elements)
      println(zipped(0).contents)
      decompress(zipped(0).contents.get) === "alert('hello');"
    }

    "write to and read from file" in {
      val elements = Seq(Element("/path.js", Some("alert('hello');")))
      val zipped = new Gzip().run(elements)
      val zippedBytes = zipped(0).contents.get
      val output : FileOutputStream  = new FileOutputStream(new File("target-file"))
      IOUtils.write(zippedBytes, output)
      val readBytes : Array[Byte] = IOUtils.toByteArray(new FileInputStream(new File("target-file")))
      decompress(readBytes) === "alert('hello');"
    }
  }

}

