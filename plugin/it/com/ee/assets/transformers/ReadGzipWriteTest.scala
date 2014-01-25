package com.ee.assets.transformers

import org.specs2.mutable.Specification
import java.util.zip.GZIPInputStream
import java.io._

class ReadGzipWriteTest extends Specification with BaseIntegration {


  "Read,Gzip,Write" should {

    val outDir = makePath("target", "test-files", "gzip-files")

    "work" in new cleanGenerated(outDir) {
      val read = new ElementReader(readFn("it"))

      val write = new ElementWriter(writeFn(outDir))

      val gzipWriter = new GzipperWriter(p => new File(makePath(outDir, p)))

      val sequence = new TransformationSequence(read, gzipWriter)

      val elements = Seq(
        Element(makePath(pkg, "js-files", "one.js"))
      )

      sequence.run(elements)

      readGzip(makePath(outDir, pkg, "js-files", "one.gz.js")).trim ===
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
