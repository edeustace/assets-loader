package com.ee.assets.transformers

import com.ee.log.Logger
import java.io.{FileOutputStream, File}
import org.apache.commons.io.IOUtils

object ByteArrayWriter {
  def apply(fileFn: String => File) = new ByteArrayWriter(fileFn).run _
}

class ByteArrayWriter(fileFn: String => File) extends Transformer[Array[Byte], Unit] {

  lazy val logger = Logger("byte-array-writer")

  override def run(elements: Seq[Element[Array[Byte]]]): Seq[Element[Unit]] = {
    for {
      e <- elements
    } yield {
      val fileOut = fileFn(e.path)
      logger.trace(s"write bytes to ${fileOut.getPath}")
      IOUtils.write(e.contents, new FileOutputStream(fileOut))
      PathElement(fileOut.getCanonicalPath)
    }
  }
}
