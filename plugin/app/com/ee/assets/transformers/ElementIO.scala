package com.ee.assets.transformers

import com.ee.log.Logger

object ElementReader {
  def apply(read: String => Option[Element[String]]) = new ElementReader(read).run _
}

class ElementReader(read: String => Option[Element[String]]) extends Transformer[Unit, String] {

  val logger = Logger("ElementReader")


  override def run(elements: Seq[Element[Unit]]): Seq[Element[String]] = {
    logger.trace(s"run: $elements")
    val out: Seq[Element[String]] = elements.map(e => read(e.path)).flatten
    out
  }
}

object ElementWriter {
  def apply(write: Element[String] => String) = new ElementWriter(write).run _
}

class ElementWriter(write: Element[String] => String) extends Transformer[String, Unit] {

  val logger = Logger("ElementWriter")

  override def run(elements: Seq[Element[String]]): Seq[PathElement] = {

    logger.trace(s"run: $elements")

    elements.map {
      e =>
        PathElement(write(e))
    }
  }
}
