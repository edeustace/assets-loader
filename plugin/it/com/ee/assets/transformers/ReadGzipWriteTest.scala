package com.ee.assets.transformers

import java.io._
import java.util.zip.GZIPInputStream
import org.specs2.mutable.Specification

class ReadGzipWriteTest extends Specification with BaseIntegration {

  "Read,Gzip,Write" should {

    val outDir = makePath("target", "test-files", "gzip-files")

    def fileFn(path: String) = {
      val f = new File(makePath(outDir, path))
      f.getParentFile.mkdirs()
      f
    }

    "work" in new cleanGenerated(outDir) {
      val read = ElementReader(readFn("it"))
      val gzip = Gzip()
      val write = ByteArrayWriter(fileFn)

      val elements = Seq(
        PathElement(makePath(pkg, "js-files", "one.js"))
      )

      val combi = read andThen gzip andThen write

      val processed = combi(elements)

      readGzip(processed(0).path).trim ===
        """var x = function(){
          |}
          | """.stripMargin.trim
    }
  }

  private def readGzip(path: String): String = {

    val is: InputStream = new FileInputStream(path)
    val gzIs: InputStream = new GZIPInputStream(is)
    val decoder: Reader = new InputStreamReader(gzIs, "UTF-8")
    val buffered: BufferedReader = new BufferedReader(decoder)

    val s = new StringBuilder
    do {
      s.append(buffered.readLine)
      s.append(System.getProperty("line.separator"))
    } while (buffered.readLine() != null)

    s.toString
  }


}
