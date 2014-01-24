package com.ee.assets.transformers

import com.ee.log.Logger

class ElementReader(read: String => Option[String]) extends Transformer {

  val logger = Logger("ElementReader")
  override def run(elements: Seq[Element]): Seq[Element] = {

    logger.trace(s"run: $elements")

    elements.map {
      e =>
        Element(e.path, read(e.path))
    }
  }
}

class ElementWriter(write: Element => String) extends Transformer {

  val logger = Logger("ElementWriter")

  override def run(elements: Seq[Element]): Seq[Element] = {

    logger.trace(s"run: $elements")

    elements.map {
      e =>
        Element(write(e), None)
    }
  }
}
