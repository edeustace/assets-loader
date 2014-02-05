package com.ee.assets.transformers

import org.specs2.mutable.Specification
import java.util.zip.GZIPInputStream
import java.io._

class ReadGzipWriteTest extends Specification with BaseIntegration {

  "Read,Gzip,Write" should {

    val outDir = makePath("target", "test-files", "gzip-files")

    def fileFn(path:String) = {
      val f = new File(makePath(outDir, path))
      f.getParentFile.mkdirs()
      f
    }

    "work" in new cleanGenerated(outDir) {
      val read = new ElementReader(readFn("it"))
      val gzip = new Gzip()
      val write = new ByteArrayWriter(fileFn)

      val elements = Seq(
        Element[String](makePath(pkg, "js-files", "one.js"))
      )

      val combi = read.run _ andThen gzip.run _ andThen write.run _

      val processed = combi(elements)

      readGzip(processed(0).path).trim ===
        """var x = function(){
          |}
          |""".stripMargin.trim
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
