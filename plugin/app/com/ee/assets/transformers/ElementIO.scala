package com.ee.assets.transformers

import com.ee.log.Logger

class ElementReader(read: String => Option[String]) extends Transformer[String,String] {

  val logger = Logger("ElementReader")
  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    logger.trace(s"run: $elements")

    elements.map {
      e =>
        Element(e.path, read(e.path))
    }
  }
}

class ElementWriter(write: Element[String] => String) extends Transformer[String,String] {

  val logger = Logger("ElementWriter")

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    logger.trace(s"run: $elements")

    elements.map {
      e =>
        Element[String](write(e), None)
    }
  }
}
