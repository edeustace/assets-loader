package com.ee.assets.transformers

import com.ee.log.Logger

class ElementReader(read: String => Option[Element[String]]) extends Transformer[Unit, String] {

  val logger = Logger("ElementReader")


  override def run(elements: Seq[Element[Unit]]): Seq[Element[String]] = {
    logger.trace(s"run: $elements")
    val out: Seq[Element[String]] = elements.map(e => read(e.path)).flatten
    out
  }
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
