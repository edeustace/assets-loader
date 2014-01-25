package com.ee.assets.transformers

import com.ee.log.Logger
import java.io.{OutputStreamWriter, BufferedWriter, FileOutputStream, File}
import java.util.zip.GZIPOutputStream

/**
 * Gzips the writes to file.
 * TODO: Would be better to have only a Gzipper that outputs an OutputStream, but we'll need
 * to look at setting up a type system for the transformers for that.
 * @param fileFn
 */
class GzipperWriter( fileFn : String => File) extends Transformer {

  lazy val logger = Logger("gzipper")

  override def run(elements: Seq[Element]): Seq[Element] = {

    elements.map {
      e =>
        e.contents.map {
          c =>
            val (name, suffix) = com.ee.utils.file.nameAndSuffix(e.path)
            val nameOut = s"$name.gz.$suffix"
            gzip(c, nameOut)
            Element(nameOut, None)
        }.getOrElse {
          logger.warn(s" ${e.path} has no contents")
          e
        }
    }
  }

  def gzip(s: String, filePath: String): File = {

    val fileOut = fileFn(filePath)
    fileOut.getParentFile.mkdirs()

    logger.debug(s"file: ${fileOut.getAbsolutePath}")
    fileOut.delete
    val outputStream = new FileOutputStream(fileOut)
    val zip = new GZIPOutputStream(outputStream)
    val writer = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"))
    val data: Array[String] = s.split("\n")

    for (line <- data) {
      writer.append(line)
      writer.newLine()
    }

    writer.close()
    new File(filePath)
  }
}
