package com.ee.assets.transformers

import com.ee.log.Logger
import java.io.{FileOutputStream, File}
import org.apache.commons.io.IOUtils

class ByteArrayWriter(fileFn : String => File) extends Transformer[Array[Byte],String]{

  lazy val logger = Logger("byte-array-writer")

  override def run(elements: Seq[Element[Array[Byte]]]): Seq[Element[String]] = {
    for{
      e <- elements
      c <- e.contents
    } yield {
      val fileOut = fileFn(e.path)
      logger.trace(s"write bytes to ${fileOut.getPath}")
      IOUtils.write(c, new FileOutputStream(fileOut))
      Element[String](fileOut.getCanonicalPath, None)
    }
  }
}
